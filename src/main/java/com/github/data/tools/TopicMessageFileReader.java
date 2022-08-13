package com.github.data.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 20:03
 * @description
 */
public class TopicMessageFileReader {

    public static void main(String[] args) throws IOException {
        String filePath = "E:\\github\\my-projects\\data_platform\\data\\20220813\\topic01_20220813.dat";
        FileInputStream fis = new FileInputStream(filePath);

        FileChannel fileChannel = fis.getChannel();
        System.out.println("file size=" + fileChannel.size());
        ByteBuffer buff = ByteBuffer.allocate((int)fileChannel.size());
        fileChannel.read(buff);
        buff.flip();

        while(buff.hasRemaining()){
            System.out.println("offset=" + buff.getInt());
            int len = buff.getInt();
            System.out.println("len=" + len);
            byte[] data = new byte[len];
            buff.get(data);
            System.out.println(new String(data));
        }

        fis.close();
    }
}
