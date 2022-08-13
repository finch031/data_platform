package com.github.data.io;

import com.github.data.json.JsonObject;
import com.github.data.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 16:04
 * @description
 */
public class TopicMessageFileWriter extends AbstractMessageFileWriter{
    private final int cacheFlushSize;
    private final List<byte[]> appendingWriteCaches = new ArrayList<>();
    private final OutputStream os;
    private static final AtomicInteger offset = new AtomicInteger(0);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public TopicMessageFileWriter(int cacheFlushSize,OutputStream os){
        this.cacheFlushSize = cacheFlushSize;
        this.os = os;
    }

    @Override
    public void write(JsonObject json, byte[] value) {
        append(value);
    }

    private void append(byte[] value){
        writeLock.lock();
        try{
            appendingWriteCaches.add(value);

            if(appendingWriteCaches.size() > cacheFlushSize){
                for (byte[] bytes : appendingWriteCaches) {
                    try{
                        // 写入偏移量
                        // os.write(offset.incrementAndGet());
                        // os.write(Utils.writeUnsignedIntLE(0,offset.incrementAndGet()));
                        ByteBuffer tmpBuff = ByteBuffer.allocate(4);
                        tmpBuff.putInt(offset.incrementAndGet());
                        os.write(tmpBuff.duplicate().array());

                        // 写入字节数据数据长度
                        // os.write(bytes.length);
                        // os.write(Utils.writeUnsignedIntLE(0,bytes.length));
                        tmpBuff.flip();
                        tmpBuff.putInt(bytes.length);
                        os.write(tmpBuff.duplicate().array());

                        // 写入字节数据
                        os.write(bytes);
                    }catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                }

                try{
                    os.flush();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }

                appendingWriteCaches.clear();
            }
        }finally {
            writeLock.unlock();
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
