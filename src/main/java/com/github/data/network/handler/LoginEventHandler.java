package com.github.data.network.handler;

import com.github.data.network.reactor.AbstractNioChannel;
import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.ApiKeys;
import com.github.data.protocol.ServerResponseCode;
import com.github.data.protocol.request.LoginRequest;
import com.github.data.protocol.request.LoginResponse;
import com.github.data.protocol.types.Struct;
import com.github.data.utils.AppConfiguration;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 19:56
 * @description
 */
public class LoginEventHandler implements EventHandler{
    private static final Map<String,String> authDataMap;

    static {
        authDataMap = AppConfiguration.loadFromPropertiesResource("server_auth.properties").toMap();
    }

    @Override
    public void handle(AbstractNioChannel channel, SelectionKey key, ApiKeys apiKeys, AbstractRequest request) {
        if(request instanceof LoginRequest){
            String user = ((LoginRequest) request).getUser();
            String passWord = ((LoginRequest) request).getPassword();

            ServerResponseCode responseCode;
            if(authDataMap.containsKey(user) && passWord.equals(authDataMap.get(user))){
                // 登录成功
                responseCode = ServerResponseCode.LOGIN_SUCCESS;

                // 新增会话
                channel.getChannelManager().addSession(key.channel());
            }else{
                responseCode = ServerResponseCode.LOGIN_FAILED;
            }

            LoginResponse loginResponse = new LoginResponse(responseCode.getValue(), responseCode.getDescriptor());
            Struct responseStruct = loginResponse.toStruct();
            ByteBuffer buffer = ByteBuffer.allocate(responseStruct.sizeOf() + 4 + 4);

            // 写入4字节的消息体长度
            buffer.putInt(responseStruct.sizeOf());

            // 写入4字节的消息类型id
            buffer.putInt(ApiKeys.LOGIN.getId());

            // 写入消息体数据
            responseStruct.writeTo(buffer);

            // prepare for write.
            buffer.flip();

            // 写socket
            channel.write(buffer, key);

            channel.getChannelManager().printChannelManagerStatus();
            System.out.println("- - - - - login event handler - - - - - -");
        }

    }
}
