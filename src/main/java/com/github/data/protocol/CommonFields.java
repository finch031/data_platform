package com.github.data.protocol;

import com.github.data.protocol.types.Field;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 17:29
 * @description
 */
public class CommonFields {

    public static final Field.Str LOGIN_USER_REQUEST = new Field.Str("login_user","Login User Name.");
    public static final Field.Str LOGIN_PASSWORD_REQUEST = new Field.Str("login_password","Login Password.");

    public static final Field.Int32 LOGIN_STATUS_CODE_RESPONSE = new Field.Int32("login_status_code","login response code.");
    public static final Field.Str LOGIN_STATUS_DESCRIPTOR_RESPONSE = new Field.Str("login_status_descriptor","login response code.");

    public static final Field.Str HEART_BEAT_MESSAGE_REQUEST = new Field.Str("heart_beat_message","heart beat message.");
    public static final Field.Int64 HEART_BEAT_TS_REQUEST = new Field.Int64("heart_beat_ts","heart beat ts.");

    public static final Field.Str HEART_BEAT_MESSAGE_RESPONSE = new Field.Str("heart_beat_message","heart beat message.");
    public static final Field.Int64 HEART_BEAT_TS_RESPONSE = new Field.Int64("heart_beat_ts","heart beat ts.");

    public static final Field.Bytes MESSAGE_KEY_REQUEST = new Field.Bytes("message key","message");
    public static final Field.Bytes MESSAGE_VALUE_REQUEST = new Field.Bytes("message value","message");
    public static final Field.Int64 MESSAGE_TS = new Field.Int64("message ts","message ts");

    public static final Field.Int32 MESSAGE_STATUS_CODE_RESPONSE = new Field.Int32("message_status_code","message response code.");
    public static final Field.Str MESSAGE_STATUS_DESCRIPTOR_RESPONSE = new Field.Str("message_status_descriptor","message response code.");


}
