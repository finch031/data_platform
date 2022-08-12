package com.github.data.protocol;

import com.github.data.protocol.request.*;
import com.github.data.protocol.types.Schema;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 14:21
 * @description
 */
public enum ApiKeys {

    LOGIN(0,"Login", LoginRequest.schema, LoginResponse.schema),

    HEART_BEAT(1,"heart_beat", HeartBeatRequest.schema, HeartBeatResponse.schema),

    MESSAGE(2,"message", MessageRequest.schema,MessageResponse.schema);

    private final short id;
    private final String name;
    private final Schema requestSchema;
    private final Schema responseSchema;

    private static final ApiKeys[] ID_TO_TYPE;
    private static final int MIN_API_KEY = 0;
    public static final int MAX_API_KEY;

    static {
        int maxKey = -1;
        for (ApiKeys key : ApiKeys.values()) {
            maxKey = Math.max(maxKey,key.id);
        }

        ApiKeys[] idToType = new ApiKeys[maxKey + 1];
        for (ApiKeys key : ApiKeys.values()) {
            idToType[key.id] = key;
        }
        ID_TO_TYPE = idToType;
        MAX_API_KEY = maxKey;
    }

    ApiKeys(int id, String name,Schema requestSchema,Schema responseSchema){
        this.id = (short) id;
        this.name = name;
        this.requestSchema = requestSchema;
        this.responseSchema = responseSchema;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Schema getRequestSchema() {
        return requestSchema;
    }

    public Schema getResponseSchema() {
        return responseSchema;
    }

    public static ApiKeys forId(int id) {
        if (!hasId(id))
            throw new IllegalArgumentException(String.format("Unexpected ApiKeys id `%s`, it should be between `%s` " +
                    "and `%s` (inclusive)", id, MIN_API_KEY, MAX_API_KEY));
        return ID_TO_TYPE[id];
    }

    public static boolean hasId(int id) {
        return id >= MIN_API_KEY && id <= MAX_API_KEY;
    }
}