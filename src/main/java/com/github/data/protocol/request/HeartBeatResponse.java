package com.github.data.protocol.request;

import com.github.data.protocol.AbstractResponse;
import com.github.data.protocol.CommonFields;
import com.github.data.protocol.types.Schema;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 21:33
 * @description
 */
public class HeartBeatResponse extends AbstractResponse {
    private final String message;
    private final long ts;

    public static final Schema schema = new Schema(CommonFields.HEART_BEAT_MESSAGE_RESPONSE,CommonFields.HEART_BEAT_TS_RESPONSE);

    public HeartBeatResponse(String message, long ts){
        this.message = message;
        this.ts = ts;
    }

    public HeartBeatResponse(Struct struct){
        this.message = struct.get(CommonFields.HEART_BEAT_MESSAGE_RESPONSE);
        this.ts = struct.get(CommonFields.HEART_BEAT_TS_RESPONSE);
    }

    public String getMessage() {
        return message;
    }

    public long getTs() {
        return ts;
    }

    public static HeartBeatResponse parse(ByteBuffer byteBuffer){
        return new HeartBeatResponse(schema.read(byteBuffer));
    }

    @Override
    public Struct toStruct() {
        Struct struct = new Struct(schema);
        struct.set(CommonFields.HEART_BEAT_MESSAGE_RESPONSE,message);
        struct.set(CommonFields.HEART_BEAT_TS_RESPONSE,ts);
        return struct;
    }
}
