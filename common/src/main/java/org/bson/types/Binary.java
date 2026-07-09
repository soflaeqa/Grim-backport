package org.bson.types;

public class Binary {
    private final byte type;
    private final byte[] data;

    public Binary(byte[] data) {
        this((byte) 0x00, data);
    }

    public Binary(byte type, byte[] data) {
        this.type = type;
        this.data = data == null ? new byte[0] : data.clone();
    }

    public byte getType() {
        return type;
    }

    public byte[] getData() {
        return data.clone();
    }
}
