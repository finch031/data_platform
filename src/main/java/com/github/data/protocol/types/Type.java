package com.github.data.protocol.types;

import com.github.data.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 16:36
 * @description
 */
public abstract class Type {
    /**
     * Write the typed object to the buffer
     *
     * @throws SchemaException If the object is not valid for its type
     */
    public abstract void write(ByteBuffer buffer, Object o);

    /**
     * Read the typed object from the buffer
     *
     * @throws SchemaException If the object is not valid for its type
     */
    public abstract Object read(ByteBuffer buffer);

    /**
     * Validate the object. If succeeded return its typed object.
     *
     * @throws SchemaException If validation failed
     */
    public abstract Object validate(Object o);

    /**
     * Return the size of the object in bytes
     */
    public abstract int sizeOf(Object o);

    /**
     * Check if the type supports null values
     * @return whether or not null is a valid value for the type implementation
     */
    public boolean isNullable() {
        return false;
    }

    /**
     * A Type that can return its description for documentation purposes.
     */
    public static abstract class DocumentedType extends Type {

        /**
         * Short name of the type to identify it in documentation;
         * @return the name of the type
         */
        public abstract String typeName();

        /**
         * Documentation of the Type.
         *
         * @return details about valid values, representation
         */
        public abstract String documentation();

        @Override
        public String toString() {
            return typeName();
        }
    }

    /**
     * The Boolean type represents a boolean value in a byte by using
     * the value of 0 to represent false, and 1 to represent true.
     *
     * If for some reason a value that is not 0 or 1 is read,
     * then any non-zero value will return true.
     */
    public static final DocumentedType BOOLEAN = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            if ((Boolean) o)
                buffer.put((byte) 1);
            else
                buffer.put((byte) 0);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            byte value = buffer.get();
            return value != 0;
        }

        @Override
        public int sizeOf(Object o) {
            return 1;
        }

        @Override
        public String typeName() {
            return "BOOLEAN";
        }

        @Override
        public Boolean validate(Object item) {
            if (item instanceof Boolean)
                return (Boolean) item;
            else
                throw new SchemaException(item + " is not a Boolean.");
        }

        @Override
        public String documentation() {
            return "Represents a boolean value in a byte. " +
                    "Values 0 and 1 are used to represent false and true respectively. " +
                    "When reading a boolean value, any non-zero value is considered true.";
        }
    };

    public static final DocumentedType INT8 = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            buffer.put((Byte) o);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            return buffer.get();
        }

        @Override
        public int sizeOf(Object o) {
            return 1;
        }

        @Override
        public String typeName() {
            return "INT8";
        }

        @Override
        public Byte validate(Object item) {
            if (item instanceof Byte)
                return (Byte) item;
            else
                throw new SchemaException(item + " is not a Byte.");
        }

        @Override
        public String documentation() {
            return "Represents an integer between -2<sup>7</sup> and 2<sup>7</sup>-1 inclusive.";
        }
    };

    public static final DocumentedType INT16 = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            buffer.putShort((Short) o);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            return buffer.getShort();
        }

        @Override
        public int sizeOf(Object o) {
            return 2;
        }

        @Override
        public String typeName() {
            return "INT16";
        }

        @Override
        public Short validate(Object item) {
            if (item instanceof Short)
                return (Short) item;
            else
                throw new SchemaException(item + " is not a Short.");
        }

        @Override
        public String documentation() {
            return "Represents an integer between -2<sup>15</sup> and 2<sup>15</sup>-1 inclusive. " +
                    "The values are encoded using two bytes in network byte order (big-endian).";
        }
    };

    public static final DocumentedType INT32 = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            buffer.putInt((Integer) o);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            return buffer.getInt();
        }

        @Override
        public int sizeOf(Object o) {
            return 4;
        }

        @Override
        public String typeName() {
            return "INT32";
        }

        @Override
        public Integer validate(Object item) {
            if (item instanceof Integer)
                return (Integer) item;
            else
                throw new SchemaException(item + " is not an Integer.");
        }

        @Override
        public String documentation() {
            return "Represents an integer between -2<sup>31</sup> and 2<sup>31</sup>-1 inclusive. " +
                    "The values are encoded using four bytes in network byte order (big-endian).";
        }
    };

    public static final DocumentedType INT64 = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            buffer.putLong((Long) o);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            return buffer.getLong();
        }

        @Override
        public int sizeOf(Object o) {
            return 8;
        }

        @Override
        public String typeName() {
            return "INT64";
        }

        @Override
        public Long validate(Object item) {
            if (item instanceof Long)
                return (Long) item;
            else
                throw new SchemaException(item + " is not a Long.");
        }

        @Override
        public String documentation() {
            return "Represents an integer between -2<sup>63</sup> and 2<sup>63</sup>-1 inclusive. " +
                    "The values are encoded using eight bytes in network byte order (big-endian).";
        }
    };

    public static final DocumentedType STRING = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            byte[] bytes = Utils.utf8((String) o);
            if (bytes.length > Short.MAX_VALUE)
                throw new SchemaException("String length " + bytes.length + " is larger than the maximum string length.");
            buffer.putShort((short) bytes.length);
            buffer.put(bytes);
        }

        @Override
        public String read(ByteBuffer buffer) {
            short length = buffer.getShort();
            if (length < 0)
                throw new SchemaException("String length " + length + " cannot be negative");
            if (length > buffer.remaining())
                throw new SchemaException("Error reading string of length " + length + ", only " + buffer.remaining() + " bytes available");
            String result = Utils.utf8(buffer, length);
            buffer.position(buffer.position() + length);
            return result;
        }

        @Override
        public int sizeOf(Object o) {
            return 2 + Utils.utf8Length((String) o);
        }

        @Override
        public String typeName() {
            return "STRING";
        }

        @Override
        public String validate(Object item) {
            if (item instanceof String)
                return (String) item;
            else
                throw new SchemaException(item + " is not a String.");
        }

        @Override
        public String documentation() {
            return "Represents a sequence of characters. First the length N is given as an " + INT16 +
                    ". Then N bytes follow which are the UTF-8 encoding of the character sequence. " +
                    "Length must not be negative.";
        }
    };

    public static final DocumentedType BYTES = new DocumentedType() {
        @Override
        public void write(ByteBuffer buffer, Object o) {
            ByteBuffer arg = (ByteBuffer) o;
            int pos = arg.position();
            buffer.putInt(arg.remaining());
            buffer.put(arg);
            arg.position(pos);
        }

        @Override
        public Object read(ByteBuffer buffer) {
            int size = buffer.getInt();
            if (size < 0)
                throw new SchemaException("Bytes size " + size + " cannot be negative");
            if (size > buffer.remaining())
                throw new SchemaException("Error reading bytes of size " + size + ", only " + buffer.remaining() + " bytes available");

            ByteBuffer val = buffer.slice();
            val.limit(size);
            buffer.position(buffer.position() + size);
            return val;
        }

        @Override
        public int sizeOf(Object o) {
            ByteBuffer buffer = (ByteBuffer) o;
            return 4 + buffer.remaining();
        }

        @Override
        public String typeName() {
            return "BYTES";
        }

        @Override
        public ByteBuffer validate(Object item) {
            if (item instanceof ByteBuffer)
                return (ByteBuffer) item;
            else
                throw new SchemaException(item + " is not a java.nio.ByteBuffer.");
        }

        @Override
        public String documentation() {
            return "Represents a raw sequence of bytes. First the length N is given as an " + INT32 +
                    ". Then N bytes follow.";
        }
    };



}
