package edu.colostate.vchill.socket;

import edu.colostate.vchill.ChillDefines;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Utility functions for VCHILL's Socket Module
 *
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2010-08-30
 */
public final class SocketUtil {
    /**
     * @param source the array to read from
     * @param offset location in source of the highest byte
     * @return the int read
     */
    public static long readUnsignedInt(final byte[] source, final int offset) {
        requireSourceLength(source, offset, ChillDefines.INT_BYTE_SIZE);
        return ((source[offset] & 0xff) << 24) |
                ((source[offset + 1] & 0xff) << 16) |
                ((source[offset + 2] & 0xff) << 8) |
                ((source[offset + 3] & 0xff));
    }

    /**
     * @param source the array to read from
     * @param offset location in source of the highest byte
     * @return the int read
     */
    public static int readInt(final byte[] source, final int offset) {
        requireSourceLength(source, offset, ChillDefines.INT_BYTE_SIZE);
        return ((source[offset] & 0xff) << 24) |
                ((source[offset + 1] & 0xff) << 16) |
                ((source[offset + 2] & 0xff) << 8) |
                ((source[offset + 3] & 0xff));
    }

    /**
     * @param source the array to read from
     * @param offset location in source of the highest byte
     * @return the short read
     */
    public static short readShort(final byte[] source, final int offset) {
        requireSourceLength(source, offset, ChillDefines.SHORT_BYTE_SIZE);
        return (short) (((source[offset] & 0xff) << 8) |
                ((source[offset + 1] & 0xff)));
    }

    /**
     * @param source the array to read from
     * @param offset location in source of the highest byte
     * @return the float read
     */
    public static float readFloat(final byte[] source, final int offset) {
        requireSourceLength(source, offset, ChillDefines.FLOAT_BYTE_SIZE);
        return Float.intBitsToFloat(readInt(source, offset));
    }

    /**
     * @param data        the float to translate
     * @param destination the array to write to
     * @param offset      location where highest byte goes
     * @return the number of bytes written
     */
    public static int writeFloat(final float data, final byte[] destination, int offset) {
        return writeInt(Float.floatToIntBits(data), destination, offset);
    }

    /**
     * @param source the array to read from
     * @param offset location in source of the highest byte
     * @return the float read
     */
    public static String readString(final byte[] source, final int offset, final int length) {
        requireSourceLength(source, offset, length);
        try {
            return new String(source, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.err.println(uee + "\nSwitching to default encoding");
            return new String(source, offset, length);
        }
    }

    /**
     * @param data        the String to translate
     * @param length      the length (number of bytes) to write
     * @param destination the array to write to
     * @param offset      location where highest byte goes
     * @return the number of bytes written
     */
    public static int writeString(final String data, final int length, final byte[] destination, int offset) {
        requireDestinationLength(destination, offset, length);
        byte[] src;
        try {
            src = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.err.println(uee + "\nSwitching to default encoding");
            src = data.getBytes();
        }
        for (int i = 0; i < length; ++i) {
            destination[offset + i] = i < src.length - 1 ? src[i] : 0;
        }
        return length;
    }

    /**
     * @param data        the int to translate
     * @param destination the array to write to
     * @param offset      location where highest byte goes
     * @return the number of bytes written
     */
    public static int writeInt(final int data, final byte[] destination, int offset) {
        requireDestinationLength(destination, offset, ChillDefines.INT_BYTE_SIZE);
        destination[offset++] = (byte) (data >> 24);
        destination[offset++] = (byte) (data >> 16);
        destination[offset++] = (byte) (data >> 8);
        destination[offset] = (byte) (data);
        return ChillDefines.INT_BYTE_SIZE;
    }

    /**
     * @param data        the short to translate
     * @param destination the array to write to
     * @param offset      location where highest byte goes
     * @return the number of bytes written
     */
    public static int writeShort(final int data, final byte[] destination, int offset) {
        requireDestinationLength(destination, offset, ChillDefines.SHORT_BYTE_SIZE);
        destination[offset++] = (byte) (data >> 8);
        destination[offset] = (byte) (data);
        return ChillDefines.SHORT_BYTE_SIZE;
    }

    /**
     * @param source      the byte array to copy from
     * @param sOffset     offset in the source array (ie first byte copied is source[sOffset]
     * @param destination the array to copy into
     * @param dOffset     offset in the destination array (ie first byte overwritten is destination[dOffset]
     * @param length      the number of bytes to copy
     * @return the number of bytes copied
     */
    public static int copyBytes(final byte[] source, final int sOffset,
                                final byte[] destination, final int dOffset, final int length) {
        requireSourceLength(source, sOffset, length);
        requireDestinationLength(destination, dOffset, length);
        for (int i = 0; i < length; ++i) {
            destination[i + dOffset] = source[i + sOffset];
        }
        return length;
    }

    /**
     * Helper method to check the Contract precondition of read methods
     */
    private static void requireSourceLength(final byte[] source, final int offset, final int length) {
        if (source.length < offset + length - 1) {
            throw new IllegalArgumentException("Not enough data available in source array:\n have " + source.length + " bytes, need " + (offset + length));
        }
    }

    /**
     * Helper method to check the Contract precondition of write methods
     */
    private static void requireDestinationLength(final byte[] destination, final int offset, final int length) {
        if (destination.length < offset + length - 1) {
            throw new IllegalArgumentException("Not enough room available in destination array:\n have " + destination.length + " bytes, need " + (offset + length));
        }
    }

    public static long readUnsignedInt(final DataInput in) throws IOException {
        return in.readInt();
    }

    public static void writeUnsignedInt(final long data, final DataOutput out) throws IOException {
        out.writeInt((int) data);
    }

    public static String readString(final DataInput in, final int length) {
        try {
            byte bytes[] = new byte[length];
            in.readFully(bytes);
            return new String(bytes, "UTF-8").trim();
        } catch (IOException ioe) {
            throw new Error(ioe);
        }
    }

    public static void writeString(final String data, final DataOutput out, final int length) {
        try {
            byte[] bytes = data.getBytes("UTF-8");
            assert bytes.length <= length;
            out.write(bytes);
            if (bytes.length < length) out.write(new byte[length - bytes.length]); //pad
        } catch (IOException ioe) {
            throw new Error(ioe);
        }
    }
}
