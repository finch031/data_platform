package com.github.data.json;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:31
 * @description
 */
public final class JsonException extends RuntimeException {
    private static final long serialVersionUID = -1576186399665327535L;

    public JsonException() {
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }
}