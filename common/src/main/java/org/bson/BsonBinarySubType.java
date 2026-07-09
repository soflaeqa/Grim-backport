package org.bson;

public enum BsonBinarySubType {
    BINARY((byte) 0x00),
    UUID_STANDARD((byte) 0x04);

    private final byte value;

    BsonBinarySubType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
