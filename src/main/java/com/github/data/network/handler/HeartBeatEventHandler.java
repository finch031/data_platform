package com.github.data.network.handler;

import com.github.data.network.reactor.AbstractNioChannel;
import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.ApiKeys;
import com.github.data.protocol.request.HeartBeatRequest;
import com.github.data.protocol.request.HeartBeatResponse;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 21:45
 * @description
 */
public class HeartBeatEventHandler implements EventHandler{
    private static final long HEART_BEAT_TIMEOUT_MAX_MILLIS = 10 * 1000;

    @Override
    public void handle(AbstractNioChannel channel, SelectionKey key, ApiKeys apiKeys, AbstractRequest request) {

        if(request instanceof HeartBeatRequest){
            HeartBeatRequest heartBeatRequest = (HeartBeatRequest) request;

            long deltaMillis = System.currentTimeMillis() - heartBeatRequest.getTs();

            HeartBeatResponse heartBeatResponse;

            if(deltaMillis > HEART_BEAT_TIMEOUT_MAX_MILLIS){
                // 超时
                String message = "心跳超时:" + deltaMillis;
                heartBeatResponse = new HeartBeatResponse(message,System.currentTimeMillis());
            }else{
                String message = "心跳正常:" + deltaMillis;
                heartBeatResponse = new HeartBeatResponse(message,System.currentTimeMillis());
            }

            // 更新会话.
            channel.getChannelManager().updateSession(key.channel());

            Struct responseStruct = heartBeatResponse.toStruct();

            ByteBuffer buffer = ByteBuffer.allocate(responseStruct.sizeOf() + 4 + 4);

            // 写入4字节的消息体长度
            buffer.putInt(responseStruct.sizeOf());

            // 写入4字节的消息类型id
            buffer.putInt(ApiKeys.HEART_BEAT.getId());

            // 写入消息体数据
            responseStruct.writeTo(buffer);

            // prepare for write.
            buffer.flip();

            // 写socket
            channel.write(buffer, key);

            channel.getChannelManager().printChannelManagerStatus();
            System.out.println("- - - - - - - - - - -");
        }
    }
}
