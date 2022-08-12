package com.github.data.protocol;

import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 14:28
 * @description
 */
public abstract class AbstractRequestResponse {

    public static ByteBuffer serialize(Struct headerStruct, Struct bodyStruct) {
        ByteBuffer buffer = ByteBuffer.allocate(headerStruct.sizeOf() + bodyStruct.sizeOf());
        headerStruct.writeTo(buffer);
        bodyStruct.writeTo(buffer);
        buffer.rewind();
        return buffer;
    }
}
