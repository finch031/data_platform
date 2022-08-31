package com.github.data.io;

import com.github.data.common.BufferPoolAllocator;
import com.github.data.common.LogManager;
import com.github.data.common.TinyLogger;
import com.github.data.json.JsonObject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 16:04
 * @description
 */
public class TopicMessageFileWriter extends AbstractMessageFileWriter{
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();
    private final int cacheFlushSize;
    private final List<byte[]> appendingWriteCaches = new ArrayList<>();
    private final OutputStream os;
    private static final AtomicInteger offset = new AtomicInteger(0);
    private final BufferPoolAllocator bufferPoolAllocator = BufferPoolAllocator.getInstance();
    private final ReentrantLock lock = new ReentrantLock();

    public TopicMessageFileWriter(int cacheFlushSize,OutputStream os){
        this.cacheFlushSize = cacheFlushSize;
        this.os = os;
    }

    @Override
    public void write(JsonObject json, byte[] value) {
        append(value);
    }

    private void append(byte[] value){
        lock.lock();
        try{
            appendingWriteCaches.add(value);

            if(appendingWriteCaches.size() > cacheFlushSize){
                ByteBuffer tmpBuff = bufferPoolAllocator.allocate(256);
                for (byte[] bytes : appendingWriteCaches) {
                    try{
                        tmpBuff.clear();
                        // 写入偏移量
                        tmpBuff.putInt(offset.incrementAndGet());
                        // 写入字节数据数据长度
                        tmpBuff.putInt(bytes.length);

                        os.write(tmpBuff.array());
                        // 写入字节数据
                        os.write(bytes);
                    }catch (IOException ioe){
                        LOG.error(ioe);
                    }
                }

                bufferPoolAllocator.release(tmpBuff,tmpBuff.capacity());

                try{
                    os.flush();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }

                appendingWriteCaches.clear();
            }
        }finally {
            lock.unlock();
        }
    }

    public void close(){
        if(os != null){
            try{
                os.close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
}
