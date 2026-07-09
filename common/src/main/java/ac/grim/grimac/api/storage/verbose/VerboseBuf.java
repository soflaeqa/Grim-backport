package ac.grim.grimac.api.storage.verbose;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public final class VerboseBuf {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final DataOutputStream data = new DataOutputStream(out);

    private byte[] source;
    private ByteArrayInputStream input;
    private DataInputStream reader;

    public VerboseBuf() {
    }

    public static VerboseBuf wrap(byte[] bytes) {
        VerboseBuf buf = new VerboseBuf();
        buf.source = bytes == null ? new byte[0] : bytes;
        buf.input = new ByteArrayInputStream(buf.source);
        buf.reader = new DataInputStream(buf.input);
        return buf;
    }

    public VerboseBuf reset() {
        out.reset();
        source = null;
        input = null;
        reader = null;
        return this;
    }

    public byte[] toByteArray() {
        if (source != null && out.size() == 0) {
            return source;
        }
        try {
            data.flush();
        } catch (IOException ignored) {
        }
        return out.toByteArray();
    }

    public int remaining() {
        if (input != null) {
            return input.available();
        }
        return toByteArray().length;
    }

    private DataInputStream reader() {
        if (reader == null) {
            source = toByteArray();
            input = new ByteArrayInputStream(source);
            reader = new DataInputStream(input);
        }
        return reader;
    }

    public double rf64() {
        try {
            return reader().readDouble();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose f64", e);
        }
    }

    public float rf32() {
        try {
            return reader().readFloat();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose f32", e);
        }
    }

    public int rvi() {
        try {
            return reader().readInt();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose int", e);
        }
    }

    public int rzz() {
        return rvi();
    }

    public long rvl() {
        try {
            return reader().readLong();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose long", e);
        }
    }

    public boolean rbool() {
        try {
            return reader().readBoolean();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose bool", e);
        }
    }

    public String rstr() {
        try {
            return reader().readUTF();
        } catch (EOFException e) {
            throw new UnderflowException("verbose payload truncated", e);
        } catch (IOException e) {
            throw new UnderflowException("failed to read verbose string", e);
        }
    }

    void writeDouble(double value) {
        try {
            data.writeDouble(value);
        } catch (IOException ignored) {
        }
    }

    void writeFloat(float value) {
        try {
            data.writeFloat(value);
        } catch (IOException ignored) {
        }
    }

    void writeInt(int value) {
        try {
            data.writeInt(value);
        } catch (IOException ignored) {
        }
    }

    void writeLong(long value) {
        try {
            data.writeLong(value);
        } catch (IOException ignored) {
        }
    }

    void writeBoolean(boolean value) {
        try {
            data.writeBoolean(value);
        } catch (IOException ignored) {
        }
    }

    void writeString(String value) {
        try {
            data.writeUTF(value == null ? "" : value);
        } catch (IOException ignored) {
        }
    }

    public static final class UnderflowException extends RuntimeException {
        public UnderflowException(String message) {
            super(message);
        }

        public UnderflowException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
