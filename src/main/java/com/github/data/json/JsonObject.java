package com.github.data.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:38
 * @description
 */
public final class JsonObject extends JsonValue implements Iterable<Map.Entry<String, JsonValue>> {
    private final Map<String, JsonValue> values;

    JsonObject() {
        this(new HashMap<>());
    }

    private JsonObject(Map<String, JsonValue> values) {
        this.values = values;
    }

    @Override
    public Iterator<Map.Entry<String, JsonValue>> iterator() {
        return values.entrySet().iterator();
    }

    @Override
    public JsonValue copy() {
        Map<String, JsonValue> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> entry : this.values.entrySet()) {
            values.put(entry.getKey(), entry.getValue().copy());
        }
        return new JsonObject(values);
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    public int size() {
        return values.size();
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public JsonValue get(String key) {
        return values.get(key);
    }

    public boolean getBoolean(String key) {
        return values.get(key).asBoolean();
    }

    public byte getByte(String key) {
        return values.get(key).asByte();
    }

    public short getShort(String key) {
        return values.get(key).asShort();
    }

    public int getInt(String key) {
        return values.get(key).asInt();
    }

    public long getLong(String key) {
        return values.get(key).asLong();
    }

    public float getFloat(String key) {
        return values.get(key).asFloat();
    }

    public double getDouble(String key) {
        return values.get(key).asDouble();
    }

    public Number getNumber(String key) {
        return values.get(key).asNumber();
    }

    public String getString(String key) {
        return values.get(key).asString();
    }

    public JsonArray getArray(String key) {
        return values.get(key).asArray();
    }

    public JsonObject getObject(String key) {
        return values.get(key).asObject();
    }

    public JsonObject add(String key, Boolean value) {
        values.put(key, value == null ? Json.Null : Json.bool(value));
        return this;
    }

    public JsonObject add(String key, Number value) {
        values.put(key, value == null ? Json.Null : Json.number(value));
        return this;
    }

    public JsonObject add(String key, String value) {
        values.put(key, value == null ? Json.Null : Json.string(value));
        return this;
    }

    public JsonObject add(String key, JsonValue value) {
        values.put(key, value == null ? Json.Null : value);
        return this;
    }

    public JsonValue remove(String key) {
        return values.remove(key);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof JsonObject && values.equals(((JsonObject) obj).values));
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "JsonObject(" + values + ")";
    }
}