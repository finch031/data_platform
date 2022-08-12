package com.github.data.protocol.request;

import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.CommonFields;
import com.github.data.protocol.types.Schema;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 17:10
 * @description 登录请求
 */
public class LoginRequest extends AbstractRequest {
    private final String user;
    private final String password;

    public static final Schema schema = new Schema(CommonFields.LOGIN_USER_REQUEST,CommonFields.LOGIN_PASSWORD_REQUEST);

    public LoginRequest(String user,String password){
        this.user = user;
        this.password = password;
    }

    public LoginRequest(Struct struct){
        this.user = struct.get(CommonFields.LOGIN_USER_REQUEST);
        this.password = struct.get(CommonFields.LOGIN_PASSWORD_REQUEST);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public static LoginRequest parse(ByteBuffer buffer){
        return new LoginRequest(schema.read(buffer));
    }

    @Override
    public Struct toStruct() {
        Struct struct = new Struct(schema);
        struct.set(CommonFields.LOGIN_USER_REQUEST,user);
        struct.set(CommonFields.LOGIN_PASSWORD_REQUEST,password);
        return struct;
    }
}
