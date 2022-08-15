package com.github.data.tools;

import com.github.data.common.BufferPoolAllocator;
import com.github.data.network.reactor.ChannelManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/15 14:02
 * @description
 */
public class ServerMonitor extends TimerTask {
    private final ChannelManager channelManager;
    private final BufferPoolAllocator bufferPoolAllocator = BufferPoolAllocator.getInstance();
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ServerMonitor(ChannelManager channelManager){
        this.channelManager = channelManager;
        isRunning.set(true);
    }

    @Override
    public void run() {
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");

        bufferPoolAllocator.printStatus();

        channelManager.printChannelManagerStatus();

        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }

    public static void start(ChannelManager channelManager){
        if(isRunning.get()){
            throw new RuntimeException("ServerMonitor已运行...");
        }
        Timer timer = new Timer("ServerMonitorThread");
        timer.scheduleAtFixedRate(new ServerMonitor(channelManager),100,15 * 1000);
    }
}
