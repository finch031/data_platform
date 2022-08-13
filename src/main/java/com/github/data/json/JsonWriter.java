package com.github.data.json;

import java.io.*;
import java.util.*;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 15:03
 * @description
 */
public final class JsonWriter {
    private static final String[] Escapes = new String[128];

    static {
        for (int i = 0x00; i <= 0x1f; i++) {
            Escapes[i] = String.format("\\u%04x", i);
        }
        Escapes['"'] = "\\\"";
        Escapes['\\'] = "\\\\";
        Escapes['\b'] = "\\b";
        Escapes['\f'] = "\\f";
        Escapes['\n'] = "\\n";
        Escapes['\r'] = "\\r";
        Escapes['\t'] = "\\t";
    }

    private final Writer writer;

    public JsonWriter(Writer writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    public void write(JsonValue value) throws IOException {
        if (value == null || value.isNull()) {
            nul();
        } else if (value.isBoolean()) {
            bool(value.asBoolean());
        } else if (value.isNumber()) {
            number(value.asNumber());
        } else if (value.isString()) {
            string(value.asString());
        } else if (value.isArray()) {
            array(value.asArray());
        } else if (value.isObject()) {
            object(value.asObject());
        }
    }

    private void nul() throws IOException {
        writer.write("null");
    }

    private void bool(boolean value) throws IOException {
        writer.write(value ? "true" : "false");
    }

    private void number(Number value) throws IOException {
        long l = value.longValue();
        double d = value.doubleValue();
        if (l != d) {
            if (!Double.isFinite(d)) {
                throw new IllegalStateException("Cannot serialize NaN or Infinity");
            }
            writer.write(Double.toString(d));
        } else {
            writer.write(Long.toString(l));
        }
    }

    private void string(String value) throws IOException {
        writer.write('"');

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 128) {
                String escape = Escapes[c];
                if (escape != null) {
                    writer.write(escape);
                    continue;
                }
            }
            writer.write(c);
        }

        writer.write('"');
    }

    private void array(JsonArray array) throws IOException {
        Iterator<JsonValue> it = array.iterator();
        if (!it.hasNext()) {
            writer.write("[]");
            return;
        }

        writer.write('[');
        while (true) {
            JsonValue value = it.next();
            write(value);
            if (!it.hasNext()) {
                writer.write(']');
                return;
            }
            writer.write(',');
        }
    }

    private void object(JsonObject object) throws IOException {
        Iterator<Map.Entry<String, JsonValue>> it = object.iterator();
        if (!it.hasNext()) {
            writer.write("{}");
            return;
        }

        writer.write('{');
        while (true) {
            Map.Entry<String, JsonValue> entry = it.next();
            string(entry.getKey());
            writer.write(':');
            write(entry.getValue());
            if (!it.hasNext()) {
                writer.write('}');
                return;
            }
        }
    }
}
