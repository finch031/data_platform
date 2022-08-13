package com.github.data.json;

import java.util.Objects;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:40
 * @description
 */
public final class JsonString extends JsonValue {
    private final String value;

    public JsonString(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public JsonValue copy() {
        return this;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof JsonString && value.equals(((JsonString) obj).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "JsonString('" + value + "')";
    }
}