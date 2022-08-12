package com.github.data.network.reactor;

import com.github.data.common.LogManager;
import com.github.data.common.TinyLogger;
import com.github.data.network.handler.EventHandler;
import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.ApiKeys;
import com.github.data.protocol.types.Struct;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 16:12
 * @description
 */
public class ApiBasedChannelHandler implements ChannelHandler{
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();

    private final List<EventHandler> handlers = new ArrayList<>();

    public ApiBasedChannelHandler(){
    }

    public void addEventHandler(EventHandler handler){
        handlers.add(handler);
    }

    @Override
    public void handleChannelRead(AbstractNioChannel channel, Object readObject, SelectionKey key) {

        try{
            if(readObject instanceof ByteBuffer){
                ByteBuffer inputBuff = (ByteBuffer) readObject;
                // buff长度
                int len = inputBuff.getInt();

                // API id
                int api = inputBuff.getInt();
                ApiKeys apiKeys = ApiKeys.forId(api);

                Struct struct = apiKeys.getRequestSchema().read(inputBuff);
                AbstractRequest request = AbstractRequest.parseRequest(apiKeys,struct);

                // process EventHandler.
                for (EventHandler handler : handlers) {
                    handler.handle(channel,key,apiKeys,request);
                }
            }
        }catch (Exception ex){
            LOG.error(ex);
        }

    }
}
