package com.github.data.utils;

import java.util.BitSet;
import java.util.PrimitiveIterator;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/15 10:32
 * @description
 */
public class TimeStampBitFormat {
    private static final int MAX_KEPT_DAYS = 15;
    private final long baseOffset;
    private final long maxTimeStamp;
    private final BitSet bitSet;

    public TimeStampBitFormat(long baseOffset){
        this.baseOffset = baseOffset;
        this.maxTimeStamp = baseOffset + MAX_KEPT_DAYS * 86400;
        this.bitSet = new BitSet();
    }

    public boolean addTimeStampValue(long ts){
        if(ts < baseOffset || ts > maxTimeStamp){
            System.err.println("当前存储时间戳超时:" + ts);
            return false;
        }
        long storeValue = ts - baseOffset;
        bitSet.set((int) storeValue);
        return true;
    }

    public long restoreFromBytes(byte[] bytes, int startPosition, int windowSize){
        BitSet bitSet = BitSet.valueOf(bytes);
        if(startPosition > bitSet.cardinality() || windowSize > bitSet.cardinality()){
            throw new IllegalArgumentException("startPosition或windowSize参数无效!");
        }
        long result = baseOffset;
        int index = 0;
        PrimitiveIterator.OfInt iterator = bitSet.stream().iterator();
        while(iterator.hasNext()){
            index++;
            // 必须在while中迭代，否则结果出错.
            int nextValue = iterator.next();

            if(index == (startPosition + windowSize)){
                result += nextValue;
                break;
            }
        }
        return result;
    }

    public byte[] toBytes(){
        return bitSet.toByteArray();
    }

    public void printAll(){
        System.out.println("size=" + bitSet.size());
        System.out.println("cardinality=" + bitSet.cardinality());
        System.out.println("- - - - - - - - - - - print start - - - - - - - - - - - - - - - - - -");
        PrimitiveIterator.OfInt iterator = bitSet.stream().iterator();
        int i = 0;
        while(iterator.hasNext()){
            long value = baseOffset + iterator.next();
            System.out.println(++i + ":" + value);
        }
        System.out.println("- - - - - - - - - - - print end - - - - - - - - - - - - - - - - - -");
    }

    public static void main(String[] args){
        TimeStampBitFormat bitFormat = new TimeStampBitFormat(System.currentTimeMillis());

        for(int i = 0; i < 18_0000; i++){
            bitFormat.addTimeStampValue(System.currentTimeMillis());
            try{
                Thread.sleep(1);
            }catch (InterruptedException ie){
                ie.printStackTrace();
            }
        }

        // bitFormat.printAll();

        byte[] data = bitFormat.toBytes();

        System.out.println("bytes=" + data.length);
        // int windowSize = 60;
        // long result = bitFormat.restoreFromBytes(data,0,windowSize);
        // System.out.println(result);

    }
}
