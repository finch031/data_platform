package com.github.data.io;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 15:22
 * @description
 */
public interface MessageWriter {

    void write(byte[] key, byte[] value);

}
