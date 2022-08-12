package com.github.data.protocol.types;

import java.nio.ByteBuffer;

/**
 * Represents a type for an array of a particular type
 */
public class ArrayOf extends Type.DocumentedType {

    private static final String ARRAY_TYPE_NAME = "ARRAY";

    private final Type type;
    private final boolean nullable;

    public ArrayOf(Type type) {
        this(type, false);
    }

    public static ArrayOf nullable(Type type) {
        return new ArrayOf(type, true);
    }

    private ArrayOf(Type type, boolean nullable) {
        this.type = type;
        this.nullable = nullable;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public void write(ByteBuffer buffer, Object o) {
        if (o == null) {
            buffer.putInt(-1);
            return;
        }

        Object[] objs = (Object[]) o;
        int size = objs.length;
        buffer.putInt(size);

        for (Object obj : objs)
            type.write(buffer, obj);
    }

    @Override
    public Object read(ByteBuffer buffer) {
        int size = buffer.getInt();
        if (size < 0 && isNullable())
            return null;
        else if (size < 0)
            throw new SchemaException("Array size " + size + " cannot be negative");

        if (size > buffer.remaining())
            throw new SchemaException("Error reading array of size " + size + ", only " + buffer.remaining() + " bytes available");
        Object[] objs = new Object[size];
        for (int i = 0; i < size; i++)
            objs[i] = type.read(buffer);
        return objs;
    }

    @Override
    public int sizeOf(Object o) {
        int size = 4;
        if (o == null)
            return size;

        Object[] objs = (Object[]) o;
        for (Object obj : objs)
            size += type.sizeOf(obj);
        return size;
    }

    public Type type() {
        return type;
    }

    @Override
    public String toString() {
        return ARRAY_TYPE_NAME + "(" + type + ")";
    }

    @Override
    public Object[] validate(Object item) {
        try {
            if (isNullable() && item == null)
                return null;

            Object[] array = (Object[]) item;
            for (Object obj : array)
                type.validate(obj);
            return array;
        } catch (ClassCastException e) {
            throw new SchemaException("Not an Object[].");
        }
    }

    @Override
    public String typeName() {
        return ARRAY_TYPE_NAME;
    }

    @Override
    public String documentation() {
        return "Represents a sequence of objects of a given type T. " +
                "Type T can be either a primitive type (e.g. " + STRING + ") or a structure. " +
                "First, the length N is given as an " + INT32 + ". Then N instances of type T follow. " +
                "A null array is represented with a length of -1. " +
                "In protocol documentation an array of T instances is referred to as [T].";
    }
}
