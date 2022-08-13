package com.github.data.json;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:37
 * @description
 */
public final class JsonNull extends JsonValue {
    static final JsonNull Null = new JsonNull();

    private JsonNull() {
    }

    @Override
    public JsonValue copy() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JsonNull;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "JsonNull()";
    }
}