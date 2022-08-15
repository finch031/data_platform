package com.github.data.common;

import com.github.data.utils.Utils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/15 12:07
 * @description
 */
public final class BufferPoolAllocator {
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();
    private static final long ONE_MB = 1024 * 1024;

    private static final Map<ChuckSize,FixedBufferPool> bufferPoolMap = new HashMap<>();

    enum ChuckSize{
        SIZE_QUARTER_KB(256,16 * ONE_MB),
        SIZE_HALF_KB(512,16 * ONE_MB),
        SIZE_1KB(1024,16 * ONE_MB),
        SIZE_2KB(2 * 1024,16 * ONE_MB),
        SIZE_4KB(4 * 1024,16 * ONE_MB),
        SIZE_8KB(8 * 1024, 16 * ONE_MB),
        SIZE_16KB(16 * 1024,16 * ONE_MB),
        SIZE_1MB(1024 * 1024, 16 * ONE_MB),
        SIZE_16MB(16 * 1024 * 1024, 128 * ONE_MB),
        SIZE_32MB(32 * 1024 * 1024, 128 * ONE_MB),
        SIZE_64MB(64 * 1024 * 1024, 256 * ONE_MB);

        private final int poolableSize;
        private final long totalMemory;

        ChuckSize(int poolableSize,long totalMemory){
            this.poolableSize = poolableSize;
            this.totalMemory = totalMemory;
        }
    }

    enum BufferPoolAllocatorHolder{
        INSTANCE;

        private final BufferPoolAllocator bufferPoolAllocator;

        BufferPoolAllocatorHolder(){
            this.bufferPoolAllocator = new BufferPoolAllocator(ChuckSize.SIZE_64MB);
        }

        public BufferPoolAllocator getInstance(){
            return bufferPoolAllocator;
        }
    }

    public static BufferPoolAllocator getInstance(){
        return BufferPoolAllocatorHolder.INSTANCE.getInstance();
    }

    private BufferPoolAllocator(ChuckSize maxChuckSize){
        if(maxChuckSize.poolableSize > ChuckSize.SIZE_64MB.poolableSize){
            throw new RuntimeException("申请的内存块大小过大:" + maxChuckSize.poolableSize);
        }

        for (ChuckSize chuckSize : ChuckSize.values()) {
            FixedBufferPool bufferPool = new FixedBufferPool(chuckSize.totalMemory,chuckSize.poolableSize);
            bufferPoolMap.put(chuckSize,bufferPool);
        }
    }

    public ByteBuffer allocate(int size, long maxTimeToBlockMs) throws InterruptedException{
        if(size <= 0){
            throw new RuntimeException("申请的内存块大小无效:" + size);
        }

        for (ChuckSize chuck : ChuckSize.values()) {
            if(chuck.poolableSize >= size){
                return bufferPoolMap.get(chuck).allocate(size,maxTimeToBlockMs);
            }
        }
        throw new RuntimeException("没有满足要求大小的内存块!");
    }

    public ByteBuffer allocate(int size){
        ByteBuffer buff;
        try{
            buff = allocate(size,500L);
        }catch (InterruptedException ie){
            LOG.error(ie);
            throw new RuntimeException("内存分配异常!");
        }
        return buff;
    }

    public void release(ByteBuffer buff, int size){
        for (ChuckSize chuckSize : ChuckSize.values()) {
            if(chuckSize.poolableSize >= size){
                bufferPoolMap.get(chuckSize).deallocate(buff,size);
                return;
            }
        }
    }

    public void printStatus(){
        for (ChuckSize chuckSize : ChuckSize.values()) {
            if(bufferPoolMap.containsKey(chuckSize)){
                FixedBufferPool bufferPool = bufferPoolMap.get(chuckSize);
                String poolableSize = Utils.formatBytes(bufferPool.poolableSize());
                String availableMemory = Utils.formatBytes(bufferPool.availableMemory());
                String unallocatedMemory = Utils.formatBytes(bufferPool.unallocatedMemory());
                System.out.println("poolableSize=" + poolableSize + ",totalMemory=" + Utils.formatBytes(chuckSize.totalMemory) + ",availableMemory=" + availableMemory + ",unallocatedMemory=" + unallocatedMemory);
            }
        }
    }
}