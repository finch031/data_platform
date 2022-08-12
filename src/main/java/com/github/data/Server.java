package com.github.data;

import com.github.data.network.handler.HeartBeatEventHandler;
import com.github.data.network.handler.LoginEventHandler;
import com.github.data.network.handler.MessageEventHandler;
import com.github.data.network.reactor.*;
import com.github.data.utils.AppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 19:34
 * @description
 */
public class Server {
    private NioReactor reactor;
    private final List<AbstractNioChannel> channels = new ArrayList<>();
    private final Dispatcher dispatcher;

    public Server(Dispatcher dispatcher){
        this.dispatcher = dispatcher;
    }

    public void start(AppConfiguration appConf) throws IOException {
        /*
         * The application can customize its event dispatching mechanism.
         */
        reactor = new NioReactor(dispatcher);

        ApiBasedChannelHandler apiBasedChannelHandler = new ApiBasedChannelHandler();

        apiBasedChannelHandler.addEventHandler(new LoginEventHandler());

        apiBasedChannelHandler.addEventHandler(new HeartBeatEventHandler());

        apiBasedChannelHandler.addEventHandler(new MessageEventHandler());

        String serviceTcpPortStr = appConf.getString("server.service.tcp.port","");
        for (String portStr : serviceTcpPortStr.split(",")) {
            reactor.registerChannel(tcpChannel(Integer.parseInt(portStr),apiBasedChannelHandler));
        }

        reactor.start();
    }

    /**
     * Stops the NIO reactor. This is a blocking call.
     *
     * @throws InterruptedException if interrupted while stopping the reactor.
     * @throws IOException          if any I/O error occurs
     */
    public void stop() throws InterruptedException, IOException {
        reactor.stop();
        dispatcher.stop();
        for (AbstractNioChannel channel : channels) {
            channel.getJavaChannel().close();
        }
    }

    private AbstractNioChannel tcpChannel(int port, ChannelHandler handler) throws IOException {
        NioServerSocketChannel channel = new NioServerSocketChannel(port, handler);
        channel.bind();
        channels.add(channel);
        return channel;
    }

    public static void main(String[] args){
        AppConfiguration appConf = AppConfiguration.loadFromPropertiesResource("server.properties");
        int reactorProcessorNum = appConf.getInteger("server.reactor.processor.num",1);
        reactorProcessorNum = reactorProcessorNum == -1 ? Runtime.getRuntime().availableProcessors() : reactorProcessorNum;

        Server server = new Server(new ThreadPoolDispatcher(reactorProcessorNum));

        try{
            server.start(appConf);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

}
