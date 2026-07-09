package ac.grim.grimac.api.storage.verbose;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class VerboseReader {
    private final DataInputStream in;

    public VerboseReader(byte[] data) {
        this.in = new DataInputStream(new ByteArrayInputStream(data == null ? new byte[0] : data));
    }

    public double rf64() {
        try {
            return in.readDouble();
        } catch (IOException e) {
            return 0.0D;
        }
    }

    public float rf32() {
        try {
            return in.readFloat();
        } catch (IOException e) {
            return 0.0F;
        }
    }

    public int rvi() {
        try {
            return in.readInt();
        } catch (IOException e) {
            return 0;
        }
    }

    public int rzz() {
        try {
            return in.readInt();
        } catch (IOException e) {
            return 0;
        }
    }

    public long rvl() {
        try {
            return in.readLong();
        } catch (IOException e) {
            return 0L;
        }
    }

    public boolean rbool() {
        try {
            return in.readBoolean();
        } catch (IOException e) {
            return false;
        }
    }

    public String rstr() {
        try {
            return in.readUTF();
        } catch (IOException e) {
            return "";
        }
    }

    public void skip(int tag) {
        if (tag == VerboseSchema.TypeTag.F64.tag()) {
            rf64();
        } else if (tag == VerboseSchema.TypeTag.F32.tag()) {
            rf32();
        } else if (tag == VerboseSchema.TypeTag.VI.tag()) {
            rvi();
        } else if (tag == VerboseSchema.TypeTag.ZZ.tag()) {
            rzz();
        } else if (tag == VerboseSchema.TypeTag.VL.tag()) {
            rvl();
        } else if (tag == VerboseSchema.TypeTag.BOOL.tag()) {
            rbool();
        } else if (tag == VerboseSchema.TypeTag.STR.tag()) {
            rstr();
        }
    }

    public void skip(VerboseSchema.TypeTag tag) {
        if (tag != null) skip(tag.tag());
    }
}
