package com.github.data.common;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A pool of ByteBuffers kept under a given memory limit.
 * This class is fairly specific to the needs of the producer.
 * In particular it has the following properties:
 * <ol>
 * <li>There is a special "poolable size" and buffers of this
 *     size are kept in a free list and recycled
 * <li>It is fair. That is all memory is given to the longest waiting
 *     thread until it has sufficient memory. This prevents starvation
 *     or deadlock when a thread asks for a large chunk of memory and
 *     needs to block until multiple buffers are deallocated.
 * </ol>
 *
 * BufferPool内存总量=已用空间 + 可用空间
 * 可用空间=未申请未使用空间 + 已申请未使用空间
 * 已申请未使用空间=Deque<ByteBuffer>
 *
 * This class is copied from Apache Project of Kafka.
 */
public final class FixedBufferPool {
    // 记录了整个Pool的大小
    private final long totalMemory;

    // BufferPool对象只针对特定大小(poolableSize指定)的ByteBuffer进行管理
    private final int poolableSize;

    // 多线程并发分配和回收ByteBuffer，所以使用锁控制并发保证线程安全
    private final ReentrantLock lock;

    // 缓存了指定大小的ByteBuffer对象
    private final Deque<ByteBuffer> free;

    // 记录因申请不到足够空间而阻塞的线程，此队列中实际记录的是阻塞线程对应的Condition对象
    private final Deque<Condition> waiters;

    /**
     * Total available memory is the sum of nonPooledAvailableMemory
     * and the number of byte buffers in free * poolableSize.
     *
     * 标识非池化的可用内存空间大小即未申请未使用空间
     * */
    private long nonPooledAvailableMemory;

    /**
     * Create a new buffer pool
     * @param memory The maximum amount of memory that this buffer pool can allocate
     * @param poolableSize The buffer size to cache in the free list rather than deallocating
     */
    public FixedBufferPool(long memory, int poolableSize){
        this.poolableSize = poolableSize;
        this.lock = new ReentrantLock();
        this.free = new ArrayDeque<>();
        this.waiters = new ArrayDeque<>();
        this.totalMemory = memory;
        this.nonPooledAvailableMemory = memory;
    }

    /**
     * Allocate a buffer of the given size. This method blocks if there is not enough memory and the buffer pool
     * is configured with blocking mode.
     *
     * @param size The buffer size to allocate in bytes
     * @param maxTimeToBlockMs The maximum time in milliseconds to block for buffer memory to be available
     * @return The buffer
     * @throws InterruptedException If the thread is interrupted while blocked
     * @throws IllegalArgumentException if size is larger than the total memory controlled by the pool (and hence we would block
     *         forever)
     */
    public ByteBuffer allocate(int size, long maxTimeToBlockMs) throws InterruptedException {
        if (size > this.totalMemory)
            throw new IllegalArgumentException("Attempt to allocate " + size
                    + " bytes, but there is a hard limit of "
                    + this.totalMemory
                    + " on memory allocations.");

        ByteBuffer buffer = null;
        this.lock.lock();
        try {
            // check if we have a free buffer of the right size pooled
            if (size == poolableSize && !this.free.isEmpty()){
                // 申请的内存空间大小size刚好等于受管理的free缓存区大小
                // 使用完毕释放后会再次回到free缓存区
                return this.free.pollFirst();
            }

            // now check if the request is immediately satisfiable with the
            // memory on hand or if we need to block
            int freeListSize = freeSize() * this.poolableSize;
            if (this.nonPooledAvailableMemory + freeListSize >= size) {
                // we have enough unallocated or pooled memory to immediately
                // satisfy the request, but need to allocate the buffer
                freeUp(size); // 循环释放free中的buffer直到满足nonPooledAvailableMemory+本次释放的内存空间大于请求大小size为止。
                this.nonPooledAvailableMemory -= size;
                // System.out.println("non pooled available memory:" + nonPooledAvailableMemory);

                // 因为内存请求大小size并不等于poolableSize，
                // 本次申请的内存大小并不会进行缓存管理且释放后
                // 也不会进入缓存free中，而是由JVM负责回收。
            } else {
                // we are out of memory and will have to block
                // 本次申请的内存空间大小size大于nonPooledAvailableMemory + freeListSize之和
                // 需要阻塞等待free(已用空间)释放正在使用中的部分内存满足请求大小
                int accumulated = 0;
                Condition moreMemory = this.lock.newCondition();
                try {
                    long remainingTimeToBlockNs = TimeUnit.MILLISECONDS.toNanos(maxTimeToBlockMs);

                    System.out.println("remaining time to block Ns: " + remainingTimeToBlockNs);

                    this.waiters.addLast(moreMemory);

                    // loop over and over until we have a buffer or have reserved
                    // enough memory to allocate one
                    while (accumulated < size) { // 边等待，边分配
                        long startWaitNs = nanoseconds();
                        long timeNs;
                        boolean waitingTimeElapsed;
                        try {
                            waitingTimeElapsed = !moreMemory.await(remainingTimeToBlockNs, TimeUnit.NANOSECONDS);
                        } finally {
                            long endWaitNs = nanoseconds();
                            timeNs = Math.max(0L, endWaitNs - startWaitNs);
                            recordWaitTime(timeNs);
                        }

                        // 等待已超时
                        if (waitingTimeElapsed) {
                            throw new RuntimeException("Failed to allocate memory within the configured max blocking time " + maxTimeToBlockMs + " ms.");
                        }

                        remainingTimeToBlockNs -= timeNs;

                        // check if we can satisfy this request from the free list,
                        // otherwise allocate memory
                        if (accumulated == 0 && size == this.poolableSize && !this.free.isEmpty()) {
                            // 此时，已有其它线程释放已使用的free内存空间，且申请的内存大小size满足poolableSize，则直接分配即可。
                            // just grab a buffer from the free list
                            buffer = this.free.pollFirst();
                            accumulated = size;
                        } else {
                            // we'll need to allocate memory, but we may only get
                            // part of what we need on this iteration
                            freeUp(size - accumulated);
                            int got = (int) Math.min(size - accumulated, this.nonPooledAvailableMemory);
                            this.nonPooledAvailableMemory -= got;
                            accumulated += got;
                        }
                    }
                    // Don't reclaim memory on throwable since nothing was thrown
                    accumulated = 0;
                } finally {
                    // When this loop was not able to successfully terminate don't loose available memory
                    this.nonPooledAvailableMemory += accumulated;
                    this.waiters.remove(moreMemory);
                }
            }
        } finally {
            // signal any additional waiters if there is more memory left
            // over for them
            try {
                if (!(this.nonPooledAvailableMemory == 0 && this.free.isEmpty()) && !this.waiters.isEmpty())
                    this.waiters.peekFirst().signal();
            } finally {
                // Another finally... otherwise find bugs complains
                lock.unlock();
            }
        }

        if (buffer == null){
            // 执行堆内存分配，用于内存请求大小size不等于poolableSize的情形。
            return safeAllocateByteBuffer(size);
        } else{
            return buffer;
        }
    }

    // Protected for testing
    protected void recordWaitTime(long timeNs) {
        System.out.println("waiting time: " + timeNs + " nano seconds.");
    }

    /**
     * Allocate a buffer.  If buffer allocation fails (e.g. because of OOM) then return the size count back to
     * available memory and signal the next waiter if it exists.
     */
    private ByteBuffer safeAllocateByteBuffer(int size) {
        boolean error = true;
        try {
            ByteBuffer buffer = allocateByteBuffer(size);
            error = false;
            return buffer;
        } finally {
            if (error) {
                this.lock.lock();
                try {
                    this.nonPooledAvailableMemory += size;
                    if (!this.waiters.isEmpty())
                        this.waiters.peekFirst().signal();
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    protected ByteBuffer allocateByteBuffer(int size) {
        return ByteBuffer.allocate(size);
    }

    /**
     * Attempt to ensure we have at least the requested number of
     * bytes of memory for allocation by deallocating pooled
     * buffers (if needed)
     */
    private void freeUp(int size) {
        // 优先nonPooledAvailableMemory中请求size大小的内存空间，若不足size大小则
        // 会从free中释放ByteBuffer，直到原nonPooledAvailableMemory大小 + 本次
        // 从free中已释放的ByteBuffer的大小超过size则停止释放free.
        while (!this.free.isEmpty() && this.nonPooledAvailableMemory < size){
            this.nonPooledAvailableMemory += this.free.pollLast().capacity();
        }
    }

    /**
     * Return buffers to the pool. If they are of the poolable size add them to the free list, otherwise just mark the
     * memory as free.
     *
     * @param buffer The buffer to return
     * @param size The size of the buffer to mark as deallocated, note that this may be smaller than buffer.capacity
     *             since the buffer may re-allocate itself during in-place compression
     */
    public void deallocate(ByteBuffer buffer, int size) {
        lock.lock();
        try {
            if (size == this.poolableSize && size == buffer.capacity()) {
                // 回收受管控的，大小等于poolableSize的那部分内存
                buffer.clear();
                this.free.add(buffer);
            } else {
                // 不等于poolableSize的内存由JVM自动回收
                this.nonPooledAvailableMemory += size;
            }

            // 每当有ByteBuffer释放时，取出waiters队首的Condition调用signal将对应线程唤醒。
            Condition moreMem = this.waiters.peekFirst();
            if (moreMem != null)
                moreMem.signal();
        } finally {
            lock.unlock();
        }
    }

    public void deallocate(ByteBuffer buffer) {
        deallocate(buffer, buffer.capacity());
    }

    /**
     * the total free memory both unallocated and in the free list
     */
    public long availableMemory() {
        lock.lock();
        try {
            return this.nonPooledAvailableMemory + freeSize() * (long) this.poolableSize;
        } finally {
            lock.unlock();
        }
    }

    // Protected for testing.
    protected int freeSize() {
        return this.free.size();
    }

    /**
     * Get the unallocated memory (not in the free list or in use)
     */
    public long unallocatedMemory() {
        lock.lock();
        try {
            return this.nonPooledAvailableMemory;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The number of threads blocked waiting on memory
     */
    public int queued() {
        lock.lock();
        try {
            return this.waiters.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * The buffer size that will be retained in the free list after use
     */
    public int poolableSize() {
        return this.poolableSize;
    }

    /**
     * The total memory managed by this pool
     */
    public long totalMemory() {
        return this.totalMemory;
    }

    // package-private method used only for testing
    Deque<Condition> waiters() {
        return this.waiters;
    }

    private long nanoseconds() {
        return System.nanoTime();
    }
}
