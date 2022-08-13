package com.github.data.io;

import com.github.data.common.DataPlatformException;
import com.github.data.utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:04
 * @description
 */
public final class MessageWriterManager {
    private static final String MESSAGE_FILE_SUFFIX = ".dat";
    private final static Map<String, TopicMessageFileWriter> topicOutStreamMap = new HashMap<>();

    private final int cacheFlushSize;
    private final String storagePath;

    public MessageWriterManager(int cacheFlushSize, String storagePath){
        this.cacheFlushSize = cacheFlushSize;
        this.storagePath = storagePath;
    }

    public synchronized TopicMessageFileWriter byTopic(String topic){
        TopicMessageFileWriter topicMessageFileWriter;
        if(topicOutStreamMap.containsKey(topic)){
            topicMessageFileWriter = topicOutStreamMap.get(topic);
        }else {
            String fileName = newMessageFileName(topic);
            try{
                Utils.mkdirIfPossible(storagePath + "/" + Utils.currDateStr());
                FileOutputStream fos = new FileOutputStream(fileName);
                topicMessageFileWriter = new TopicMessageFileWriter(cacheFlushSize,fos);
                topicOutStreamMap.put(topic,topicMessageFileWriter);
            }catch (IOException ioe){
                throw new DataPlatformException("初始化topic文件失败!");
            }
        }
        return topicMessageFileWriter;
    }

    public void close(){
        topicOutStreamMap.forEach((k,v) -> {
            v.close();
        });
    }

    private String newMessageFileName(String topic){
        String fileName = topic.toLowerCase() + "_" + Utils.timestampToDateTime(System.currentTimeMillis(),"yyyyMMdd") + MESSAGE_FILE_SUFFIX;
        return storagePath + "/" + Utils.currDateStr() + "/" + fileName;
    }
}
