package com.github.data.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/13 13:46
 * @description
 */
public class JsonReader {
    private static final int ObjectStart = 1;
    private static final int ObjectEnd = 2;
    private static final int ArrayStart = 3;
    private static final int ArrayEnd = 4;
    private static final int Colon = 5;
    private static final int Comma = 6;
    private static final int String = 7;
    private static final int Number = 8;
    private static final int True = 9;
    private static final int False = 10;
    private static final int Null = 11;
    private static final int Eof = 12;

    private final StringBuilder builder = new StringBuilder();
    private final Reader reader;

    private int current;
    private int token;
    private String value;

    public JsonReader(Reader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
        next();
    }

    public JsonValue parse() {
        JsonValue result;
        try {
            nextToken();
            result = parseValue();
        } catch (StackOverflowError e) {
            throw new JsonException("Stack overflow");
        }
        nextToken();
        if (token != Eof) {
            throw new JsonException("Not a single JSON document");
        }
        return result;
    }

    private JsonValue parseValue() {
        switch (token) {
            case ObjectStart:
                return parseObject();
            case ArrayStart:
                return parseArray();
            case String:
                return Json.string(value);
            case Number:
                return new JsonNumber(value);
            case True:
                return Json.bool(true);
            case False:
                return Json.bool(false);
            case Null:
                return Json.Null;
            default:
                throw new JsonException("Unexpected " + token);
        }
    }

    private JsonObject parseObject() {
        // Skip leading ObjectStart
        nextToken();

        JsonObject object = Json.object();
        while (token != ObjectEnd) {
            if (object.size() > 0) {
                verify(Comma);
            }
            String key = verify(String);
            verify(Colon);
            object.add(key, parseValue());
            nextToken();
        }
        return object;
    }

    private JsonArray parseArray() {
        // Skip leading ArrayStart
        nextToken();

        JsonArray array = Json.array();
        while (token != ArrayEnd) {
            if (array.size() > 0) {
                verify(Comma);
            }
            array.add(parseValue());
            nextToken();
        }
        return array;
    }

    private String verify(int expected) {
        if (token != expected) {
            throw new JsonException("Expected " + expected + ", got " + token);
        }
        String value = this.value;
        nextToken();
        return value;
    }

    public int nextToken() {
        skipWhitespace();
        switch (peek()) {
            case '{':
                next();
                return token(ObjectStart, null);
            case '}':
                next();
                return token(ObjectEnd, null);
            case '[':
                next();
                return token(ArrayStart, null);
            case ']':
                next();
                return token(ArrayEnd, null);
            case ':':
                next();
                return token(Colon, null);
            case ',':
                next();
                return token(Comma, null);
            case '"':
                return token(String, parseString());
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return token(Number, parseNumber());
            case 't':
                return token(True, expect("true"));
            case 'f':
                return token(False, expect("false"));
            case 'n':
                return token(Null, expect("null"));
            case -1:
                return token(Eof, null);
            default:
                throw new JsonException("Unexpected character '" + peek() + "'");
        }
    }

    private int token(int type, String value) {
        this.token = type;
        this.value = value;
        return type;
    }

    private String parseString() {
        // skip leading '"'
        next();
        builder.setLength(0);
        while (!isEof() && peek() != '"') {
            if (peek() < 0x20) {
                throw new JsonException("Raw control character");
            } else if (peek() == '\\') {
                next();
                switch (read()) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case '/':
                        builder.append('/');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        builder.append(parseUnicode());
                        break;
                    default:
                        throw new JsonException("Illegal escape");
                }
            } else {
                appendNext();
            }
        }
        if (peek() != '"') {
            throw new JsonException("Unclosed string literal");
        }
        next();
        return builder.toString();
    }

    private char parseUnicode() {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int read = read();
            if (!isHexDigit(read)) {
                throw new JsonException("Expected hex digit");
            }
            result = (result << 4) | Character.digit(read, 16);
        }
        return (char) result;
    }

    private String parseNumber() {
        builder.setLength(0);

        if (peek() == '-') {
            appendNext();
        }

        // integer part
        if (peek() == '0') {
            appendNext();
        } else {
            if (peek() < '1' || peek() > '9') {
                throw new JsonException();
            }
            digits();
        }

        // decimal part
        if (peek() == '.') {
            appendNext();
            digits();
        }

        // exponent part
        if (peek() == 'e' || peek() == 'E') {
            appendNext();
            if (peek() == '-' || peek() == '+') {
                appendNext();
            }
            digits();
        }

        return builder.toString();
    }

    private void digits() {
        if (!isDigit(peek())) {
            throw new JsonException("Expected a digit");
        }
        while (isDigit(peek())) {
            appendNext();
        }
    }

    private String expect(String expected) {
        for (int i = 0; i < expected.length(); i++) {
            if (read() != expected.charAt(i)) {
                throw new JsonException("Expected '" + expected + "' literal");
            }
        }
        return expected;
    }

    private void appendNext() {
        builder.appendCodePoint(read());
    }

    private void skipWhitespace() {
        while (isWhitespace(peek())) {
            next();
        }
    }

    private boolean isWhitespace(int c) {
        switch (c) {
            case '\t':
            case '\n':
            case '\r':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isHexDigit(int c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'F'
                || c >= 'a' && c <= 'f';
    }

    int peek() {
        return current;
    }

    int read() {
        int result = current;
        current = readChar();
        return result;
    }

    int next() {
        current = readChar();
        return current;
    }

    boolean isEof() {
        return current == -1;
    }

    private int readChar() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new JsonException("Unexpected I/O error", e);
        }
    }
}