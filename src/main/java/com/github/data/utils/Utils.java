package com.github.data.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 16:42
 * @description
 */
public final class Utils {

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    // prints up to 2 decimal digits. used for human readable printing
    private static final DecimalFormat TWO_DIGIT_FORMAT = new DecimalFormat("0.##");
    private static final String[] BYTE_SCALE_SUFFIXES = new String[] {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    private Utils(){
        // no instance.
    }

    /**
     * write an unsigned integer in little-endian format to a byte array
     * at a given offset.
     *
     * @param buffer the byte array to write to.
     * @param offset the position in buffer to write to.
     * @param value the value to write.
     * */
    public static void writeUnsignedIntLE(byte[] buffer, int offset, int value){
        // 0000 0000 0000 0000 0000 0000 1110 1001

        // 无符号整型强转为byte,结果就是只保留低字节的8位，也就是4字节的value的最低一个字节
        buffer[offset] = (byte) value;                  // 写入第1个字节

        // 无符号整型向右移动8位，结果就是原先的整型低位
        // 第2个字节移动到低位第一个字节位置,再进行强转，
        // 最终结果就是保留原始整型第二个字节

        buffer[offset + 1] = (byte) (value >>> 8);      // 写入第2个字节
        buffer[offset + 2] = (byte) (value >>> 16);     // 写入第3个字节
        buffer[offset + 3]   = (byte) (value >>> 24);   // 写入第4个字节
    }

    public static byte[] writeUnsignedIntLE(int offset, int value){
        byte[] buffer = new byte[4];
        // 无符号整型强转为byte,结果就是只保留低字节的8位，也就是4字节的value的最低一个字节
        buffer[offset] = (byte) value;                  // 写入第1个字节

        // 无符号整型向右移动8位，结果就是原先的整型低位
        // 第2个字节移动到低位第一个字节位置,再进行强转，
        // 最终结果就是保留原始整型第二个字节

        buffer[offset + 1] = (byte) (value >>> 8);      // 写入第2个字节
        buffer[offset + 2] = (byte) (value >>> 16);     // 写入第3个字节
        buffer[offset + 3]   = (byte) (value >>> 24);   // 写入第4个字节
        return buffer;
    }

    /**
     * Turn a string into a utf8 byte[]
     *
     * @param string The string
     * @return The byte[]
     */
    public static byte[] utf8(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get the length for UTF8-encoding a string without encoding it first
     *
     * @param s The string to calculate the length for
     * @return The length when serialized
     */
    public static int utf8Length(CharSequence s) {
        int count = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            char ch = s.charAt(i);
            if (ch <= 0x7F) {
                count++;
            } else if (ch <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }
        return count;
    }

    /**
     * Turn the given UTF8 byte array into a string
     *
     * @param bytes The byte array
     * @return The string
     */
    public static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read a UTF8 string from a byte buffer. Note that the position of the byte buffer is not affected
     * by this method.
     *
     * @param buffer The buffer to read from
     * @param length The length of the string in bytes
     * @return The UTF8 string
     */
    public static String utf8(ByteBuffer buffer, int length) {
        return utf8(buffer, 0, length);
    }

    /**
     * Read a UTF8 string from the current position till the end of a byte buffer. The position of the byte buffer is
     * not affected by this method.
     *
     * @param buffer The buffer to read from
     * @return The UTF8 string
     */
    public static String utf8(ByteBuffer buffer) {
        return utf8(buffer, buffer.remaining());
    }

    /**
     * Read a UTF8 string from a byte buffer at a given offset. Note that the position of the byte buffer
     * is not affected by this method.
     *
     * @param buffer The buffer to read from
     * @param offset The offset relative to the current position in the buffer
     * @param length The length of the string in bytes
     * @return The UTF8 string
     */
    public static String utf8(ByteBuffer buffer, int offset, int length) {
        if (buffer.hasArray())
            return new String(buffer.array(), buffer.arrayOffset() + buffer.position() + offset, length, StandardCharsets.UTF_8);
        else
            return utf8(toArray(buffer, offset, length));
    }

    /**
     * Read the given byte buffer from its current position to its limit into a byte array.
     * @param buffer The buffer to read from
     */
    public static byte[] toArray(ByteBuffer buffer) {
        return toArray(buffer, 0, buffer.remaining());
    }

    /**
     * Read a byte array from its current position given the size in the buffer
     * @param buffer The buffer to read from
     * @param size The number of bytes to read into the array
     */
    public static byte[] toArray(ByteBuffer buffer, int size) {
        return toArray(buffer, 0, size);
    }

    /**
     * Read a byte array from the given offset and size in the buffer
     * @param buffer The buffer to read from
     * @param offset The offset relative to the current position of the buffer
     * @param size The number of bytes to read into the array
     */
    public static byte[] toArray(ByteBuffer buffer, int offset, int size) {
        byte[] dest = new byte[size];
        if (buffer.hasArray()) {
            System.arraycopy(buffer.array(), buffer.position() + buffer.arrayOffset() + offset, dest, 0, size);
        } else {
            int pos = buffer.position();
            buffer.position(pos + offset);
            buffer.get(dest);
            buffer.position(pos);
        }
        return dest;
    }

    public static ThreadFactory reactorNamedDaemonThreadFactory(){
        return new ThreadFactory(){
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                Thread t = new Thread(r);
                t.setName("reactor_io_processor_" + counter.addAndGet(1));
                t.setDaemon(true);
                return t;
            }
        };
    }

    public static ThreadFactory acceptorNamedDaemonThreadFactory(){
        return new ThreadFactory(){
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                Thread t = new Thread(r);
                t.setName("reactor_acceptor_processor_" + counter.addAndGet(1));
                t.setDaemon(true);
                return t;
            }
        };
    }

    public static byte[] md5(String data){
        MessageDigest messageDigest;
        try{
            messageDigest = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException nae){
            throw new IllegalArgumentException(nae);
        }
        return messageDigest.digest(data.getBytes());
    }

    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    public static String md5Hex(final String data) {
        char[] encodeData = encodeHex(md5(data));
        return new String(encodeData);
    }

    public static void sleepQuietly(int timeout, TimeUnit timeUnit){
        try{
            timeUnit.sleep(timeout);
        }catch (InterruptedException ie){
            // ignore.
        }
    }

    public static String timestampToDateTime(long ts, String pattern){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
        // "yyyy-MM-dd HH:mm:ss.SSS"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }

    public static String currDateStr(){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return localDateTime.format(formatter);
    }

    public static void mkdirIfPossible(String dirStr){
        File dir = new File(dirStr);
        if(dir.exists()){
            return;
        }
        try{
            Files.createDirectory(Paths.get(dirStr));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /**
     * formats a byte number as a human readable String ("3.2 MB")
     * @param bytes some size in bytes
     */
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return String.valueOf(bytes);
        }
        double asDouble = (double) bytes;
        int ordinal = (int) Math.floor(Math.log(asDouble) / Math.log(1024.0));
        double scale = Math.pow(1024.0, ordinal);
        double scaled = asDouble / scale;
        String formatted = TWO_DIGIT_FORMAT.format(scaled);
        try {
            return formatted + " " + BYTE_SCALE_SUFFIXES[ordinal];
        } catch (IndexOutOfBoundsException e) {
            //huge number?
            return String.valueOf(asDouble);
        }
    }
}
