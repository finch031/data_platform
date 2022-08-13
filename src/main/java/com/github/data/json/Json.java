package com.github.data.json;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:41
 * @description
 *      This project is copied from https://github.com/jandk/tinyjson
 */
public final class Json {
    public static final JsonValue Null = JsonNull.Null;

    private Json() {
        throw new UnsupportedOperationException();
    }

    public static JsonValue bool(boolean value) {
        return value ? JsonBoolean.True : JsonBoolean.False;
    }

    public static JsonValue number(long value) {
        return new JsonNumber(value);
    }

    public static JsonValue number(double value) {
        return new JsonNumber(value);
    }

    public static JsonValue number(Number value) {
        return new JsonNumber(value);
    }

    public static JsonValue string(String value) {
        return new JsonString(value);
    }

    public static JsonArray array() {
        return new JsonArray();
    }

    public static JsonObject object() {
        return new JsonObject();
    }

    public static JsonValue parse(Reader reader) {
        return new JsonReader(reader).parse();
    }

    public static JsonValue parse(String json) {
        return parse(new StringReader(json));
    }
}