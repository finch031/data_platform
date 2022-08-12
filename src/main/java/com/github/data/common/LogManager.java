package com.github.data.common;

import com.github.data.utils.JvmPid;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/12 13:35
 * @description
 */
public final class LogManager {
    private final TinyLogger tinyLogger;
    private static final String LOG_DIR = "./logs";
    private static final String LOG_FILE_NAME_BASE = "data_platform";

    private LogManager(){
        FileWriter fr;
        try{
            fr = new FileWriter(LOG_DIR + "/" + LOG_FILE_NAME_BASE + "_" + JvmPid.getPid() + ".log");
        }catch (IOException ioe){
            throw new RuntimeException("日志文件打开失败!");
        }
        this.tinyLogger = new TinyLogger(fr);
    }

    public TinyLogger getTinyLogger(){
        return tinyLogger;
    }

    enum LogHolder{
        INSTANCE;

        private final LogManager instance;

        LogHolder(){
            this.instance = new LogManager();
            this.instance.getTinyLogger().start();
        }

        private LogManager getInstance(){
            return instance;
        }
    }

    public static LogManager getInstance(){
        return LogHolder.INSTANCE.getInstance();
    }
}
