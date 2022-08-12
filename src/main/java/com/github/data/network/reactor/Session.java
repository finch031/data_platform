package com.github.data.network.reactor;

import java.util.Objects;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 14:51
 * @description 会话
 */
public class Session {
    // 会话ID
    private final long sessionID;

    // 本地EndPoint
    private final EndPoint localEndPoint;

    // 远程EndPoint
    private final EndPoint remoteEndPoint;

    // 会话开始时间
    private final long sessionStartTs;

    // 上一次心跳消息回复时间
    private long lastHeartBeatAckTs;

    public Session(long sessionID, EndPoint localEndPoint, EndPoint remoteEndPoint){
        this.sessionID = sessionID;
        this.localEndPoint = localEndPoint;
        this.remoteEndPoint = remoteEndPoint;
        this.sessionStartTs = System.currentTimeMillis();
    }

    public void updateHeartBeatAckTs(long ts){
        this.lastHeartBeatAckTs = ts;
    }

    public long getSessionID() {
        return sessionID;
    }

    public long getLastHeartBeatAckTs() {
        return lastHeartBeatAckTs;
    }

    public long getSessionStartTs() {
        return sessionStartTs;
    }

    public EndPoint getLocalEndPoint() {
        return localEndPoint;
    }

    public EndPoint getRemoteEndPoint() {
        return remoteEndPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionID == session.sessionID && Objects.equals(localEndPoint, session.localEndPoint) && Objects.equals(remoteEndPoint, session.remoteEndPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionID, localEndPoint, remoteEndPoint, lastHeartBeatAckTs);
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionID=" + sessionID +
                ", localEndPoint=" + localEndPoint +
                ", remoteEndPoint=" + remoteEndPoint +
                ", lastHeartBeatAckTs=" + lastHeartBeatAckTs +
                '}';
    }
}
