package ac.grim.grimac.api.storage.query;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

public final class Cursors {
    public static final byte SCHEMA_ORDERED_PAIR = 1;
    public static final byte SCHEMA_TYPED_PAIR = 2;
    public static final byte TYPE_STRING = 1;
    public static final byte TYPE_BINARY = 2;
    public static final byte TYPE_DOUBLE = 3;
    public static final byte TYPE_DATE = 4;
    public static final int MAX_TYPED_ORDERED_LEN = 65535;
    public static final int MAX_PAYLOAD_BYTES = 4096;

    private Cursors() {
    }

    public static Cursor encode(long orderedKey, byte[] idBytes) {
        if (idBytes.length > 255) {
            throw new IllegalArgumentException("cursor id exceeds 255 bytes: " + idBytes.length);
        }
        ByteBuffer buf = ByteBuffer.allocate(10 + idBytes.length);
        buf.put(SCHEMA_ORDERED_PAIR);
        buf.putLong(orderedKey);
        buf.put((byte) idBytes.length);
        buf.put(idBytes);
        return new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array()));
    }

    public static Cursor encodeTyped(byte typeTag, byte[] orderedBytes, byte[] idBytes) {
        if (!isKnownTypeTag(typeTag)) {
            throw new IllegalArgumentException("unknown cursor ordered value type tag: 0x" + Integer.toHexString(typeTag & 0xff));
        }
        if (orderedBytes.length > MAX_TYPED_ORDERED_LEN) {
            throw new IllegalArgumentException("typed cursor ordered value exceeds 65535 bytes: " + orderedBytes.length);
        }
        if (idBytes.length > 255) {
            throw new IllegalArgumentException("cursor id exceeds 255 bytes: " + idBytes.length);
        }
        int total = 5 + orderedBytes.length + idBytes.length;
        if (total > MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("encoded typed cursor exceeds MAX_PAYLOAD_BYTES (4096): " + total);
        }
        ByteBuffer buf = ByteBuffer.allocate(total);
        buf.put(SCHEMA_TYPED_PAIR);
        buf.put(typeTag);
        buf.putShort((short) orderedBytes.length);
        buf.put(orderedBytes);
        buf.put((byte) idBytes.length);
        buf.put(idBytes);
        return new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array()));
    }

    public static Decoded decode(Cursor cursor) {
        byte[] bytes = base64(cursor);
        if (bytes.length < 10) {
            throw new IllegalArgumentException("cursor payload too short (" + bytes.length + " bytes)");
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        byte schema = buf.get();
        if (schema != SCHEMA_ORDERED_PAIR) {
            throw new IllegalArgumentException("unexpected cursor schema: " + schema);
        }
        long orderedKey = buf.getLong();
        int len = Byte.toUnsignedInt(buf.get());
        if (buf.remaining() != len) {
            throw new IllegalArgumentException("cursor id length mismatch: expected " + len + ", remaining " + buf.remaining());
        }
        byte[] idBytes = new byte[len];
        buf.get(idBytes);
        return new Decoded(orderedKey, idBytes);
    }

    public static DecodedTyped decodeTyped(Cursor cursor) {
        byte[] bytes = base64(cursor);
        if (bytes.length < 5) {
            throw new IllegalArgumentException("typed cursor payload too short (" + bytes.length + " bytes)");
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        byte schema = buf.get();
        if (schema != SCHEMA_TYPED_PAIR) {
            throw new IllegalArgumentException("unexpected typed cursor schema: 0x" + Integer.toHexString(schema & 0xff));
        }
        byte typeTag = buf.get();
        if (!isKnownTypeTag(typeTag)) {
            throw new IllegalArgumentException("unknown cursor ordered value type tag: 0x" + Integer.toHexString(typeTag & 0xff));
        }
        int orderedLen = Short.toUnsignedInt(buf.getShort());
        if (buf.remaining() < orderedLen + 1) {
            throw new IllegalArgumentException("typed cursor ordered length mismatch: expected " + orderedLen + ", remaining " + buf.remaining());
        }
        byte[] orderedBytes = new byte[orderedLen];
        buf.get(orderedBytes);
        int idLen = Byte.toUnsignedInt(buf.get());
        if (buf.remaining() != idLen) {
            throw new IllegalArgumentException("typed cursor id length mismatch: expected " + idLen + ", remaining " + buf.remaining());
        }
        byte[] idBytes = new byte[idLen];
        buf.get(idBytes);
        return new DecodedTyped(typeTag, orderedBytes, idBytes);
    }

    public static byte peekSchema(Cursor cursor) {
        byte[] bytes = base64(cursor);
        if (bytes.length < 1) {
            throw new IllegalArgumentException("empty cursor payload");
        }
        return bytes[0];
    }

    private static byte[] base64(Cursor cursor) {
        String token = cursor.token();
        if ((token.length() * 3L) / 4L > MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("cursor token too large: " + token.length());
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(token);
            if (bytes.length > MAX_PAYLOAD_BYTES) {
                throw new IllegalArgumentException("decoded cursor exceeds MAX_PAYLOAD_BYTES (4096): " + bytes.length);
            }
            return bytes;
        } catch (IllegalArgumentException e) {
            String sample = token.length() > 64 ? token.substring(0, 64) + "..." : token;
            throw new IllegalArgumentException("invalid cursor token: " + sample, e);
        }
    }

    private static boolean isKnownTypeTag(byte tag) {
        return tag == TYPE_STRING || tag == TYPE_BINARY || tag == TYPE_DOUBLE || tag == TYPE_DATE;
    }

    public static final class Decoded {
        private final long orderedKey;
        private final byte[] idBytes;

        public Decoded(long orderedKey, byte[] idBytes) {
            this.orderedKey = orderedKey;
            this.idBytes = idBytes;
        }

        public long orderedKey() { return orderedKey; }
        public byte[] idBytes() { return idBytes; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Decoded)) return false;
            Decoded decoded = (Decoded) o;
            return orderedKey == decoded.orderedKey && Arrays.equals(idBytes, decoded.idBytes);
        }

        @Override
        public int hashCode() {
            int result = (int) (orderedKey ^ (orderedKey >>> 32));
            result = 31 * result + Arrays.hashCode(idBytes);
            return result;
        }

        @Override
        public String toString() {
            return "Decoded[orderedKey=" + orderedKey + ", idBytes=" + Arrays.toString(idBytes) + "]";
        }
    }

    public static final class DecodedTyped {
        private final byte typeTag;
        private final byte[] orderedBytes;
        private final byte[] idBytes;

        public DecodedTyped(byte typeTag, byte[] orderedBytes, byte[] idBytes) {
            this.typeTag = typeTag;
            this.orderedBytes = orderedBytes;
            this.idBytes = idBytes;
        }

        public byte typeTag() { return typeTag; }
        public byte[] orderedBytes() { return orderedBytes; }
        public byte[] idBytes() { return idBytes; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DecodedTyped)) return false;
            DecodedTyped that = (DecodedTyped) o;
            return typeTag == that.typeTag
                    && Arrays.equals(orderedBytes, that.orderedBytes)
                    && Arrays.equals(idBytes, that.idBytes);
        }

        @Override
        public int hashCode() {
            int result = typeTag;
            result = 31 * result + Arrays.hashCode(orderedBytes);
            result = 31 * result + Arrays.hashCode(idBytes);
            return result;
        }

        @Override
        public String toString() {
            return "DecodedTyped[typeTag=" + typeTag + ", orderedBytes=" + Arrays.toString(orderedBytes) + ", idBytes=" + Arrays.toString(idBytes) + "]";
        }
    }
}
