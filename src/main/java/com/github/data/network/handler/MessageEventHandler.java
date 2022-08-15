package com.github.data.network.handler;

import com.github.data.common.BufferPoolAllocator;
import com.github.data.common.DataPlatformException;
import com.github.data.common.LogManager;
import com.github.data.common.TinyLogger;
import com.github.data.io.MessageWriter;
import com.github.data.io.MessageWriterManager;
import com.github.data.json.Json;
import com.github.data.json.JsonObject;
import com.github.data.json.JsonValue;
import com.github.data.network.reactor.AbstractNioChannel;
import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.ApiKeys;
import com.github.data.protocol.ServerResponseCode;
import com.github.data.protocol.request.MessageRequest;
import com.github.data.protocol.request.MessageResponse;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/12 14:51
 * @description
 */
public class MessageEventHandler implements EventHandler{
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();
    private final BufferPoolAllocator bufferPoolAllocator = BufferPoolAllocator.getInstance();
    private final MessageWriterManager writerManager;

    public MessageEventHandler(MessageWriterManager writerManager){
        this.writerManager = writerManager;
    }

    @Override
    public void handle(AbstractNioChannel channel, SelectionKey key, ApiKeys apiKeys, AbstractRequest request) {
        if(request instanceof MessageRequest){
            MessageRequest messageRequest = (MessageRequest) request;

            byte[] keyBytes = messageRequest.getKey();
            byte[] valueBytes = messageRequest.getValue();

            JsonValue jsonValue = Json.parse(new String(keyBytes));
            if(jsonValue.isObject()){
                JsonObject jsonObject = jsonValue.asObject();

                String processPolicy = jsonObject.getString("message_process_policy");
                if(processPolicy.equals("message_queue")){
                    String topic = jsonObject.getString("topic");
                    MessageWriter messageWriter = writerManager.byTopic(topic);
                    messageWriter.write(keyBytes,valueBytes);
                }else{
                    LOG.error("Unsupported message process policy: " + processPolicy);
                }
            }else {
                throw new DataPlatformException("Invalid message key.");
            }

            MessageResponse messageResponse = new MessageResponse(ServerResponseCode.MESSAGE_SUCCESS.getValue(),"消息接收成功");
            Struct responseStruct = messageResponse.toStruct();
            ByteBuffer buffer = bufferPoolAllocator.allocate(responseStruct.sizeOf() + 4 + 4);

            // 写入4字节的消息体长度
            buffer.putInt(responseStruct.sizeOf());

            // 写入4字节的消息类型id
            buffer.putInt(ApiKeys.MESSAGE.getId());

            // 写入消息体数据
            responseStruct.writeTo(buffer);

            // prepare for write.
            buffer.flip();

            // 写socket
            channel.write(buffer, key);

            channel.getChannelManager().printChannelManagerStatus();
            System.out.println("- - - - - message event handler - - - - - -");
        }
    }
}
