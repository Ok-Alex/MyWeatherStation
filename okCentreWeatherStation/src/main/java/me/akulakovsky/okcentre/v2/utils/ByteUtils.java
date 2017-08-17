package me.akulakovsky.okcentre.v2.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtils {

    public static short toShort(byte[] data) {
        if (data == null || data.length != 2) return 0x0;
        // ----------
        return (short)(
                (0xff & data[0]) << 8   |
                        (0xff & data[1]) << 0
        );
    }

    public static short bytesToShort(byte[] data) {
        if (data == null || data.length != 2) return  0x0;
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[0]);
        bb.put(data[1]);
        return bb.getShort(0);
    }

    public static short swapBytes(short value){
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;

        return (short) (b1 << 8 | b2 << 0);
    }

}