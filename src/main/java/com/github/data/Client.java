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
    private static class User{
        private String user;
        private String passWord;

        public User(String user,String passWord){
            this.user = user;
            this.passWord = passWord;
        }

        public String getUser() {
            return user;
        }

        public String getPassWord() {
            return passWord;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public void setPassWord(String passWord) {
            this.passWord = passWord;
        }

        @Override
        public String toString() {
            return "User{" +
                    "user='" + user + '\'' +
                    ", passWord='" + passWord + '\'' +
                    '}';
        }
    }

    private static final TinyLogger LOG = LogManager.getInstance().getTinyLogger();

    private final ExecutorService service = Executors.newFixedThreadPool(4);

    private static final Random userChooseRandom = new Random(System.currentTimeMillis());

    private static final User[] users = new User[]{
      new User("admin","2020@data"),
      new User("user1","2021@data"),
      new User("user2","2022@data")
    };

    private static final String[] keys = {
            "{\"topic\":\"topic01\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic02\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic03\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic04\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic05\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic06\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic07\", \"message_process_policy\":\"message_queue\"}",
            "{\"topic\":\"topic08\", \"message_process_policy\":\"message_queue\"}"
    };

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
        String serverHost = "node4";
        serverHost = "192.168.0.1";
        int serverPort = 16666;
        int sendNum = 1000;
        service.execute(new TcpLoggingClient(serverHost, 16666,sendNum,users[0]));
        service.execute(new TcpLoggingClient(serverHost, 17777,sendNum,users[1]));
        service.execute(new TcpLoggingClient(serverHost, 18888,sendNum,users[2]));
        // service.execute(new TcpLoggingClient(serverHost, 19999,sendNum));

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

    /**
     * A logging client that sends requests to Reactor on TCP socket.
     */
    static class TcpLoggingClient implements Runnable {
        private final int serverPort;
        private final String serverHost;
        private final int messageNum;
        private final User user;

        /**
         * Creates a new TCP logging client.
         */
        public TcpLoggingClient(String serverHost, int serverPort, int messageNum,User user) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.messageNum = messageNum;
            this.user = user;
        }

        private void heartBeat(OutputStream outputStream){
            Runnable heartTask = new Runnable() {
                @Override
                public void run() {

                    try{
                        sendHeartRequests(outputStream);
                        // readHeartResponses(socket.getInputStream());
                    }catch (IOException ioe){
                        ioe.printStackTrace();
                    }

                }
            };

            // 定时收发心跳消息
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(heartTask,100,500,TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            InetAddress address;
            try{
                // address = InetAddress.getByName(serverHost);
                address = InetAddress.getLocalHost();
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
                sendLoginRequests(outputStream,user);

                // 接收登录回复
                readLoginResponses(socket.getInputStream());

                heartBeat(outputStream);

                Random random = new Random();
                for (int i = 0; i < messageNum; i++) {
                    // 发送数据消息请求
                    sendMessageRequests(outputStream);

                    // 收取数据消息回复
                    readMessageResponses(socket.getInputStream());

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
        }

        private void sendLoginRequests(OutputStream outputStream,User user) throws IOException {
            // User user = users[userChooseRandom.nextInt(users.length)];
            System.out.println("user=" + user.toString());
            LoginRequest loginRequest = new LoginRequest(user.getUser(), Utils.md5Hex(user.getPassWord()));
            // System.out.println(loginRequest.toStruct().toString());
            int sizeOf = loginRequest.toStruct().sizeOf();
            ByteBuffer buff = ByteBuffer.allocate(sizeOf + 4 + 4);
            buff.putInt(sizeOf);
            buff.putInt(ApiKeys.LOGIN.getId());
            loginRequest.toStruct().writeTo(buff);

            outputStream.write(buff.array());
        }

        private void readLoginResponses(InputStream inputStream) throws IOException {
            byte[] data = new byte[1024];
            int read = inputStream.read(data, 0, data.length);
            if (read == 0) {
                LOG.info("Read zero bytes");
            } else {
                ByteBuffer buff = ByteBuffer.wrap(data, 0, read);
                int len = buff.getInt();
                int apiId = buff.getInt();
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
                HeartBeatResponse heartBeatResponse = HeartBeatResponse.parse(buff);
                System.out.println(heartBeatResponse.toStruct().toString());
            }
        }

        private void sendMessageRequests(OutputStream outputStream) throws IOException{
           String key = keys[userChooseRandom.nextInt(keys.length)];
           Random random = new Random(System.currentTimeMillis());
           String value = Thread.currentThread().getId() + "_" + random.nextInt(Integer.MAX_VALUE - 1);
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
                MessageResponse messageResponse = MessageResponse.parse(buff);
                System.out.println(messageResponse.toStruct().toString());
            }
        }
    }
}