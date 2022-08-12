package com.github.data.protocol;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 18:21
 * @description 服务端返回状态码
 */
public enum ServerResponseCode {

    // 登录成功
    LOGIN_SUCCESS("login_success",0,"登录成功!"),

    // 登录失败
    LOGIN_FAILED("login_failed",1,"登录失败!"),

    // 消息接收成功
    MESSAGE_SUCCESS("message_success",2,"消息接收成功!"),

    // 消息接收失败
    MESSAGE_FAILED("message_failed",3,"消息接收失败!"),



    ;

    private final String name;
    private final int value;
    private final String descriptor;

    ServerResponseCode(String name, int value, String descriptor){
        this.name = name;
        this.value = value;
        this.descriptor = descriptor;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
