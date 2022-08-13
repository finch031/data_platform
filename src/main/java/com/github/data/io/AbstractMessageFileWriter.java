package com.github.data.io;

import com.github.data.common.DataPlatformException;
import com.github.data.json.Json;
import com.github.data.json.JsonObject;
import com.github.data.json.JsonValue;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 15:46
 * @description
 */
public abstract class AbstractMessageFileWriter implements MessageWriter{

    @Override
    public final void write(byte[] key, byte[] value) {
        JsonValue jsonValue = Json.parse(new String(key));
        if(jsonValue.isObject()){
            JsonObject jsonObject = jsonValue.asObject();
            write(jsonObject,value);
        }else {
            throw new DataPlatformException("Invalid message key.");
        }
    }

    public abstract void write(JsonObject json,byte[] value);

}