package com.github.data.protocol.request;

import com.github.data.protocol.AbstractResponse;
import com.github.data.protocol.CommonFields;
import com.github.data.protocol.types.Schema;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/12 14:43
 * @description
 */
public class MessageResponse extends AbstractResponse {
    private final int statusCode;
    private final String descriptor;

    public static final Schema schema = new Schema(CommonFields.MESSAGE_STATUS_CODE_RESPONSE,CommonFields.MESSAGE_STATUS_DESCRIPTOR_RESPONSE);

    public MessageResponse(int statusCode, String descriptor){
        this.statusCode = statusCode;
        this.descriptor = descriptor;
    }

    public MessageResponse(Struct struct){
        this.statusCode = struct.get(CommonFields.MESSAGE_STATUS_CODE_RESPONSE);
        this.descriptor = struct.get(CommonFields.MESSAGE_STATUS_DESCRIPTOR_RESPONSE);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public static MessageResponse parse(ByteBuffer buffer){
        return new MessageResponse(schema.read(buffer));
    }

    @Override
    public Struct toStruct() {
        Struct struct = new Struct(schema);
        struct.set(CommonFields.MESSAGE_STATUS_CODE_RESPONSE,statusCode);
        struct.set(CommonFields.MESSAGE_STATUS_DESCRIPTOR_RESPONSE,descriptor);
        return struct;
    }
}
