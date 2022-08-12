package com.github.data.protocol.request;

import com.github.data.protocol.AbstractResponse;
import com.github.data.protocol.CommonFields;
import com.github.data.protocol.types.Schema;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 18:18
 * @description
 */
public class LoginResponse extends AbstractResponse {
    private final int statusCode;
    private final String descriptor;

    public static final Schema schema = new Schema(CommonFields.LOGIN_STATUS_CODE_RESPONSE,CommonFields.LOGIN_STATUS_DESCRIPTOR_RESPONSE);

    public LoginResponse(int statusCode, String descriptor){
        this.statusCode = statusCode;
        this.descriptor = descriptor;
    }

    public LoginResponse(Struct struct){
        this.statusCode = struct.get(CommonFields.LOGIN_STATUS_CODE_RESPONSE);
        this.descriptor = struct.get(CommonFields.LOGIN_STATUS_DESCRIPTOR_RESPONSE);
    }

    public static LoginResponse parse(ByteBuffer buffer) {
        return new LoginResponse(schema.read(buffer));
    }

    @Override
    public Struct toStruct() {
        Struct struct = new Struct(schema);
        struct.set(CommonFields.LOGIN_STATUS_CODE_RESPONSE,statusCode);
        struct.set(CommonFields.LOGIN_STATUS_DESCRIPTOR_RESPONSE,descriptor);
        return struct;
    }
}
