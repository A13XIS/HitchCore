package com.hitchh1k3rsguide.$CORE_REPLACE$.hitchcore;

import io.netty.buffer.ByteBuf;

public class Serializer
{

    public static String readString(ByteBuf buffer)
    {
        byte[] arr = new byte[buffer.readInt()];
        buffer.readBytes(arr);
        return new String(arr);
    }

    public static String[] readStringArray(ByteBuf buffer)
    {
        int size = buffer.readInt();
        String[] arr = new String[size];
        for (int i = 0; i < size; ++i)
        {
            arr[i] = readString(buffer);
        }
        return arr;
    }

    public static void writeString(ByteBuf buffer, String string)
    {
        byte[] bytes = string.getBytes();
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static void writeStringArray(ByteBuf buffer, String[] arr)
    {
        int size = arr.length;
        buffer.writeInt(size);
        for (String str : arr)
        {
            writeString(buffer, str);
        }
    }

    public static void writeBooleanArray(ByteBuf buffer, boolean[] arr)
    {
        int size = arr.length;
        buffer.writeInt(size);
        for (int i = 0; i < size; i += 8)
        {
            int flags = 0;
            for (int q = 0; q < 8 && i + q < size; ++q)
            {
                flags |= (arr[i + q] ? 1 << q : 0);
            }
            buffer.writeByte(flags);
        }
    }

    public static boolean[] readBooleanArray(ByteBuf buffer)
    {
        int size = buffer.readInt();
        boolean[] arr = new boolean[size];
        if (size > 0)
        {
            for (int i = 0; i <= (size - 1) / 8; ++i)
            {
                int flags = buffer.readByte();
                for (int q = 0; q < 8 && (i * 8) + q < size; ++q)
                {
                    arr[(i * 8) + q] = (flags & (1 << q)) > 0;
                }
            }
        }
        return arr;
    }

    public static void writeBooleanFlagByte(ByteBuf buffer, boolean... flags)
    {
        int flagMap = 0;
        for (int q = 0; q < 8 && q < flags.length; ++q)
        {
            flagMap |= (flags[q] ? 1 << q : 0);
        }
        buffer.writeByte(flagMap);
    }

    public static boolean[] readBooleanFlagByte(ByteBuf buffer)
    {
        boolean[] arr = new boolean[8];
        int flags = buffer.readByte();
        for (int q = 0; q < 8; ++q)
        {
            arr[q] = (flags & (1 << q)) > 0;
        }
        return arr;
    }

    public static int[] readIntArray(ByteBuf buffer)
    {
        int size = buffer.readInt();
        int[] arr = new int[size];
        for (int i = 0; i < size; ++i)
        {
            arr[i] = buffer.readInt();
        }
        return arr;
    }

    public static void writeIntArray(ByteBuf buffer, int[] arr)
    {
        int size = arr.length;
        buffer.writeInt(size);
        for (int i : arr)
        {
            buffer.writeInt(i);
        }
    }

    public static double[] readDoubleArray(ByteBuf buffer)
    {
        int size = buffer.readInt();
        double[] arr = new double[size];
        for (int i = 0; i < size; ++i)
        {
            arr[i] = buffer.readDouble();
        }
        return arr;
    }

    public static void writeDoubleArray(ByteBuf buffer, double[] arr)
    {
        int size = arr.length;
        buffer.writeInt(size);
        for (Double i : arr)
        {
            buffer.writeDouble(i);
        }
    }

}
