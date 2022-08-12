package com.github.data.network.handler;

import com.github.data.network.reactor.AbstractNioChannel;
import com.github.data.protocol.AbstractRequest;
import com.github.data.protocol.ApiKeys;

import java.nio.channels.SelectionKey;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 16:57
 * @description
 */
public interface EventHandler {

    void handle(AbstractNioChannel channel, SelectionKey key, ApiKeys apiKeys, AbstractRequest request);

}
