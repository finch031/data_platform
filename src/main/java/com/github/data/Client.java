package com.github.data;

import com.github.data.common.LogManager;
import com.github.data.common.TinyLogger;
import com.github.data.protocol.ApiKeys;
import com.github.data.protocol.request.*;
import com.github.data.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 13:02
 * @description
 */
public class Client {
    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();

    private final ExecutorService service = Executors.newFixedThreadPool(4);

    /**
     * App client entry.
     *
     * @throws IOException if any I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        Client appClient = new Client();
        appClient.start();
    }

    /**
     * Starts the logging clients.
     *
     * @throws IOException if any I/O error occurs.
     */
    public void start() throws IOException {
        LOG.info("Starting logging clients");
        service.execute(new TcpLoggingClient("Client 1", 16666));

        stop();
    }

    /**
     * Stops logging clients. This is a blocking call.
     */
    public void stop() {
        service.shutdown();
        if (!service.isTerminated()) {
            service.shutdownNow();
            try {
                service.awaitTermination(1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error("exception awaiting termination", e);
            }
        }
        LOG.info("Logging clients stopped");
    }

    private static void artificialDelayOf(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.error("sleep interrupted", e);
        }
    }

    /**
     * A logging client that sends requests to Reactor on TCP socket.
     */
    static class TcpLoggingClient implements Runnable {
        private final int serverPort;
        private final String clientName;

        /**
         * Creates a new TCP logging client.
         *
         * @param clientName the name of the client to be sent in logging requests.
         * @param serverPort the port on which client will send logging requests.
         */
        public TcpLoggingClient(String clientName, int serverPort) {
            this.clientName = clientName;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            String remoteHost = "node4";
            InetAddress address;
            try{
                address = InetAddress.getByName(remoteHost);
                // address = InetAddress.getLocalHost();
            }catch (UnknownHostException uhe){
                throw new RuntimeException("获取远程地址失败!");
            }

            Socket socket;
            try{
                socket = new Socket(address, serverPort);
            }catch (IOException ioe){
                throw new RuntimeException("Socket初始化失败!");
            }

            try /*(Socket socket = new Socket(address, serverPort))*/ {
                OutputStream outputStream = socket.getOutputStream();

                // 发送登录请求
                sendLoginRequests(outputStream);

                // 接收登录回复
                readResponses(socket.getInputStream());

                Runnable heartTask = new Runnable() {
                    @Override
                    public void run() {

                        try{
                            sendHeartRequests(outputStream);
                            readHeartResponses(socket.getInputStream());
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }

                    }
                };

                // 定时收发心跳消息
                // Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(heartTask,100,500,TimeUnit.MILLISECONDS);

                Random random = new Random();
                for (int i = 0; i < 1000; i++) {
                    // 发送数据消息请求
                    sendMessageRequests(outputStream);

                    // 收取数据消息回复
                    readMessageResponses(socket.getInputStream());

                    System.out.println("times=" + i);
                    try{
                        Thread.sleep(random.nextInt(100));
                    }catch (InterruptedException ie){
                        // ignore
                    }
                }
            } catch (IOException e) {
                LOG.error("error sending requests", e);
                throw new RuntimeException(e);
            }

            try{
                Thread.sleep(3 * 3600 * 1000);
            }catch (InterruptedException ie){
                // ignore
            }
        }

        private void sendLoginRequests(OutputStream outputStream) throws IOException {
            LoginRequest loginRequest = new LoginRequest("user1", Utils.md5Hex("2021@data"));
            int sizeOf = loginRequest.toStruct().sizeOf();
            ByteBuffer buff = ByteBuffer.allocate(sizeOf + 4 + 4);
            buff.putInt(sizeOf);
            buff.putInt(ApiKeys.LOGIN.getId());
            loginRequest.toStruct().writeTo(buff);
            outputStream.write(buff.array());
        }

        private void readResponses(InputStream inputStream) throws IOException {
            byte[] data = new byte[1024];
            int read = inputStream.read(data, 0, data.length);
            if (read == 0) {
                LOG.info("Read zero bytes");
            } else {
                ByteBuffer buff = ByteBuffer.wrap(data, 0, read);
                int len = buff.getInt();
                int apiId = buff.getInt();
                System.out.println("len=" + len + ",api id=" + apiId);

                LoginResponse loginResponse = LoginResponse.parse(buff);
                System.out.println(loginResponse.toStruct().toString());
            }
        }

        private void sendHeartRequests(OutputStream outputStream) throws IOException{
            HeartBeatRequest heartBeatRequest = new HeartBeatRequest("心跳消息",System.currentTimeMillis());
            int sizeOf = heartBeatRequest.toStruct().sizeOf();

            ByteBuffer buff = ByteBuffer.allocate(sizeOf + 4 + 4);
            buff.putInt(sizeOf);
            buff.putInt(ApiKeys.HEART_BEAT.getId());

            heartBeatRequest.toStruct().writeTo(buff);
            outputStream.write(buff.array());
        }

        private void readHeartResponses(InputStream inputStream) throws IOException{
            byte[] data = new byte[1024];
            int read = inputStream.read(data, 0, data.length);
            if (read == 0) {
                LOG.info("Read zero bytes");
            } else {
                ByteBuffer buff = ByteBuffer.wrap(data, 0, read);
                int len = buff.getInt();
                int apiId = buff.getInt();
                System.out.println("len=" + len + ",api id=" + apiId);

                HeartBeatResponse heartBeatResponse = HeartBeatResponse.parse(buff);
                System.out.println(heartBeatResponse.toStruct().toString());
            }
        }

        private void sendMessageRequests(OutputStream outputStream) throws IOException{
            // String key = Thread.currentThread().getName() + "_" + System.currentTimeMillis();
            String key = "{\"topic\":\"topic01\", \"message_process_policy\":\"message_queue\"}";

            Random random = new Random(System.currentTimeMillis());
            String value = Thread.currentThread().getName() + "_" + random.nextInt(Integer.MAX_VALUE - 1);
            MessageRequest messageRequest = new MessageRequest(key.getBytes(),value.getBytes(),System.currentTimeMillis());

            int sizeOf = messageRequest.toStruct().sizeOf();

            ByteBuffer buff = ByteBuffer.allocate(sizeOf + 4 + 4);
            buff.putInt(sizeOf);
            buff.putInt(ApiKeys.MESSAGE.getId());

            messageRequest.toStruct().writeTo(buff);

            outputStream.write(buff.array());
        }

        private void readMessageResponses(InputStream inputStream) throws IOException{
            byte[] data = new byte[1024];
            int read = inputStream.read(data, 0, data.length);
            if (read == 0) {
                LOG.info("Read zero bytes");
            } else {
                ByteBuffer buff = ByteBuffer.wrap(data, 0, read);
                int len = buff.getInt();
                int apiId = buff.getInt();
                System.out.println("len=" + len + ",api id=" + apiId);

                MessageResponse messageResponse = MessageResponse.parse(buff);
                System.out.println(messageResponse.toStruct().toString());
            }
        }
    }
}
