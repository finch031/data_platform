package com.github.data.common;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/7/24 15:34
 * @description
 */
public final class TinyLogger{
    private static final int DEFAULT_LOG_QUEUE_CAPACITY = 1024;
    private final Logger logger;
    private final Thread logThread;
    private boolean isShutdown;

    public enum LogLevel{
        DEBUG("debug"),
        WARN("warn"),
        INFO("info"),
        ERROR("error");

        private final String levelName;

        LogLevel(String levelName){
            this.levelName = levelName;
        }
    }

    public interface Logger extends Runnable{
        void log(String message, String level) throws InterruptedException;

        default void debug(String message) {
            try{
                log(message,LogLevel.DEBUG.levelName);
            }catch (InterruptedException ie){
                // ignore.
            }
        }

        default void error(String message) {
            try{
                log(message,LogLevel.ERROR.levelName);
            }catch (InterruptedException ie){
                // ignore.
            }
        }

        default void info(String message) {
            try{
                log(message,LogLevel.INFO.levelName);
            }catch (InterruptedException ie){
                // ignore.
            }
        }

        default void warn(String message) {
            try{
                log(message,LogLevel.WARN.levelName);
            }catch (InterruptedException ie){
                // ignore.
            }
        }
    }

    private class DefaultLoggerTask implements Logger{
        private int reservations;
        private final BlockingQueue<String> messageQueue;
        private final PrintWriter messageWriter;

        public DefaultLoggerTask(BlockingQueue<String> messageQueue, PrintWriter messageWriter){
            this.messageQueue = messageQueue;
            this.messageWriter = messageWriter;
        }

        @Override
        public void run() {
            try{
                while (true){
                    try{
                        synchronized (TinyLogger.this){
                            if(isShutdown && reservations == 0){
                                break;
                            }
                        }
                        String message = messageQueue.take();
                        synchronized (TinyLogger.this) {
                            --reservations;
                        }
                        messageWriter.println(message);
                        messageWriter.flush();
                    }catch (InterruptedException ie){
                        // retry.
                    }
                }
            }finally {
                messageWriter.close();
            }
        }

        @Override
        public void log(String message, String level) throws InterruptedException{
            synchronized (this){
                if(isShutdown){
                    throw new IllegalStateException("日志服务已关闭异常!");
                }
                ++reservations;
            }

            String logLine = timestampToDateTime(System.currentTimeMillis()) + " [" + level.toUpperCase() + "] " + message;

            /*
             * inserts the specified element into this queue,
             * waiting if necessary for space to become available.
             * */
            messageQueue.put(logLine);
        }
    }

    public TinyLogger(int logQueueCapacity, PrintWriter writer){
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(logQueueCapacity);
        this.logger = new DefaultLoggerTask(queue,writer);
        this.logThread = new Thread(this.logger);
    }

    public TinyLogger(PrintWriter writer){
        this(DEFAULT_LOG_QUEUE_CAPACITY,writer);
    }

    public TinyLogger(FileWriter fileWriter){
        this(new PrintWriter(fileWriter));
    }

    public void start(){
        this.logThread.start();
    }

    public void stop(){
        synchronized (this){
            this.isShutdown = true;
        }
        this.logThread.interrupt();
    }

    public void info(String message){
        logger.info(message);
    }

    public void info(String message,Object argValue){
        logger.info(message + "," + argValue);
    }

    public void warn(String message){
        logger.warn(message);
    }

    public void debug(String message){
        logger.debug(message);
    }

    public void error(String message){
        logger.error(message);
    }

    public void error(Throwable ex){
        logger.error( "Error, stack trace: " + stackTrace(ex));
    }

    public void error(String message, Throwable ex){
        logger.error(message + ", stack trace: " + stackTrace(ex));
    }

    private static String timestampToDateTime(long ts){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
        return localDateTime.format(formatter);
    }

    /**
     * Get the stack trace from an exception as a string
     */
    private static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
