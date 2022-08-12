package com.github.data.network.reactor;

import com.github.data.utils.Snowflake;

import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 14:47
 * @description
 */
public final class ChannelManager {
    private final Map<EndPoint, Session> channelToSessionMap;
    private static final Snowflake snowflake = new Snowflake(6,10);

    public ChannelManager(){
        this.channelToSessionMap = new ConcurrentHashMap<>();
    }

    public void addSession(SelectableChannel channel){
        SocketChannel socketChannel = (SocketChannel) channel;
        Socket socket = socketChannel.socket();
        EndPoint localEndPoint = getLocalEndPointFromSocket(socket);
        EndPoint remoteEndPoint = getRemoteEndPointFromSocket(socket);
        Session session = new Session(snowflake.id(),localEndPoint,remoteEndPoint);
        session.updateHeartBeatAckTs(System.currentTimeMillis());
        channelToSessionMap.putIfAbsent(remoteEndPoint,session);
    }

    public void updateSession(SelectableChannel channel){
        EndPoint remoteEndPoint = getRemoteEndPointFromSocket(((SocketChannel)channel).socket());
        channelToSessionMap.get(remoteEndPoint).updateHeartBeatAckTs(System.currentTimeMillis());
    }

    public void removeSession(SelectableChannel channel){
        EndPoint remoteEndPoint = getRemoteEndPointFromSocket(((SocketChannel)channel).socket());
        channelToSessionMap.remove(remoteEndPoint);
    }

    private EndPoint getLocalEndPointFromSocket(Socket socket){
        return EndPoint.of(socket.getLocalAddress().getHostAddress(),socket.getLocalPort());
    }

    private EndPoint getRemoteEndPointFromSocket(Socket socket){
        return EndPoint.of(socket.getInetAddress().getHostAddress(),socket.getPort());
    }

    public void printChannelManagerStatus(){
        channelToSessionMap.forEach((k,v) -> {
            System.out.println(v);
        });
    }
}
