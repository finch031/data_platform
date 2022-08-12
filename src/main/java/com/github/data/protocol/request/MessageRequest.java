package com.github.data.protocol.request;

import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.CommonFields;
import com.github.data.protocol.types.Schema;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/12 14:25
 * @description
 */
public class MessageRequest extends AbstractRequest {
    private final byte[] key;
    private final byte[] value;
    private final long ts;

    public static final Schema schema = new Schema(CommonFields.MESSAGE_KEY_REQUEST,CommonFields.MESSAGE_VALUE_REQUEST,CommonFields.MESSAGE_TS);

    public MessageRequest(byte[] key, byte[] value, long ts){
        this.key = key;
        this.value = value;
        this.ts = ts;
    }

    public MessageRequest(Struct struct){
        this.key = struct.getByteArray(CommonFields.MESSAGE_KEY_REQUEST.name);
        this.value = struct.getByteArray(CommonFields.MESSAGE_VALUE_REQUEST.name);
        this.ts = struct.get(CommonFields.MESSAGE_TS);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public long getTs() {
        return ts;
    }

    public static MessageRequest parse(ByteBuffer buffer){
        return new MessageRequest(schema.read(buffer));
    }

    @Override
    public Struct toStruct() {
        Struct struct = new Struct(schema);
        struct.setByteArray(CommonFields.MESSAGE_KEY_REQUEST.name,key);
        struct.setByteArray(CommonFields.MESSAGE_VALUE_REQUEST.name,value);
        struct.set(CommonFields.MESSAGE_TS,ts);
        return struct;
    }
}
