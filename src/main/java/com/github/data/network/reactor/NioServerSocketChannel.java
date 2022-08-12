package com.github.data.network.reactor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.github.data.common.LogManager;
import com.github.data.common.TinyLogger;
import com.github.data.utils.IoBuffer;

/**
 * A wrapper over {@link NioServerSocketChannel}
 * which can read and write data on a {@link SocketChannel}.
 */
public class NioServerSocketChannel extends AbstractNioChannel {
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();

    // SocketChannel数据读取临时缓存大小
    private static final int SOCKET_CHANNEL_TMP_READ_BUFF_SIZE = 256;

    // SocketChannel数据读取缓存(不可用于并发情形)
    private static final IoBuffer SOCKET_CHANNEL_IO_BUFFER = IoBuffer.allocate(1024 * 4);

    private final int port;

    /**
     * Creates a {@link ServerSocketChannel} which will bind at provided port and use
     * <code>handler</code> to handle incoming events on this channel.
     *
     * <p>Note the constructor does not bind the socket, {@link #bind()} method should be called for
     * binding the socket.
     *
     * @param port    the port on which channel will be bound to accept incoming connection requests.
     * @param handler the handler that will handle incoming requests on this channel.
     * @throws IOException if any I/O error occurs.
     */
    public NioServerSocketChannel(int port, ChannelHandler handler) throws IOException {
        super(handler, ServerSocketChannel.open());
        this.port = port;
    }

    @Override
    public int getInterestedOps() {
        // being a server socket channel it is interested in accepting connection from remote peers.
        return SelectionKey.OP_ACCEPT;
    }

    /**
     * Get server socket channel.
     *
     * @return the underlying {@link ServerSocketChannel}.
     */
    @Override
    public ServerSocketChannel getJavaChannel() {
        return (ServerSocketChannel) super.getJavaChannel();
    }

    /**
     * Reads and returns {@link ByteBuffer} from the underlying {@link SocketChannel} represented by
     * the <code>key</code>. Due to the fact that there is a dedicated channel for each client
     * connection we don't need to store the sender.
     */
    @Override
    public ByteBuffer read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // SocketChannel数据读取缓存
        // IoBuffer ioBuffer = IoBuffer.allocate(1024 * 4);

        SOCKET_CHANNEL_IO_BUFFER.buf().clear();

        // 读取SocketChannel数据
        int readBytes = readChannel(socketChannel,SOCKET_CHANNEL_IO_BUFFER);

        if(readBytes == -1){
            throw new IOException("客户端已关闭异常!");
        }

        SOCKET_CHANNEL_IO_BUFFER.buf().flip();

        return SOCKET_CHANNEL_IO_BUFFER.buf();
    }

    /**
     * 读取SocketChannel数据
     * */
    private int readChannel(SocketChannel socketChannel,IoBuffer readBuffer) throws IOException{
        // 临时读缓存
        ByteBuffer data = ByteBuffer.allocate(SOCKET_CHANNEL_TMP_READ_BUFF_SIZE);
        int readBytes = 0;
        int ret;
        while((ret = socketChannel.read(data)) > 0){
            data.flip();
            readBytes += data.remaining();
            // readBuffer自动扩容
            readBuffer.put(data.array(),data.position(),data.remaining());
            data.clear();
        }

        return ret < 0 ? ret : readBytes;
    }


    /**
     * Binds TCP socket on the provided <code>port</code>.
     * @throws IOException if any I/O error occurs.
     */
    @Override
    public void bind() throws IOException {
        ServerSocketChannel javaChannel = getJavaChannel();
        javaChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        javaChannel.configureBlocking(false);
        LOG.info("Bound TCP socket at port: {}", port);
    }

    /**
     * Writes the pending {@link ByteBuffer} to the underlying channel sending data to the intended
     * receiver of the packet.
     */
    @Override
    protected void doWrite(Object pendingWrite, SelectionKey key) throws IOException {
        ByteBuffer pendingBuffer = (ByteBuffer) pendingWrite;
        ((SocketChannel) key.channel()).write(pendingBuffer);
    }
}