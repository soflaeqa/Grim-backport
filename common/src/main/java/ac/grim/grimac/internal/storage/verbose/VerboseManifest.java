package ac.grim.grimac.internal.storage.verbose;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public final class VerboseManifest {
    public static final int FORMAT_VERSION = 1;
    public static final int FLAVOR_UNKNOWN = 0;
    public static final int FLAVOR_V2_PUBLIC = 1;
    public static final int FLAVOR_V3_PREMIUM = 2;

    private VerboseManifest() {}

    public static byte[] textOnly(int flavor) {
        return encode(flavor, Collections.<Integer, Integer>emptyMap());
    }

    public static byte[] encode(int flavor, @NotNull Map<Integer, Integer> checkCodecVersions) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4 + checkCodecVersions.size() * 3);
        out.write(FORMAT_VERSION);
        out.write(flavor & 0xFF);
        writeUVarInt(out, 0);
        writeUVarInt(out, checkCodecVersions.size());
        int previousCheckId = 0;
        for (Map.Entry<Integer, Integer> entry : new TreeMap<Integer, Integer>(checkCodecVersions).entrySet()) {
            int checkId = entry.getKey();
            int codecVersion = entry.getValue();
            writeUVarInt(out, checkId - previousCheckId);
            writeUVarInt(out, codecVersion);
            previousCheckId = checkId;
        }
        return out.toByteArray();
    }

    public static @NotNull Decoded decode(byte[] bytes) {
        try {
            Reader in = new Reader(bytes == null ? new byte[0] : bytes);
            int format = in.readU8();
            if (format != FORMAT_VERSION) return new Decoded(format, FLAVOR_UNKNOWN, Collections.<Integer, Integer>emptyMap(), false);
            int flavor = in.readU8();
            int featureBits = in.readUVarInt();
            if (featureBits != 0) return new Decoded(format, flavor, Collections.<Integer, Integer>emptyMap(), false);
            int count = in.readUVarInt();
            Map<Integer, Integer> codecs = new LinkedHashMap<Integer, Integer>();
            int checkId = 0;
            for (int i = 0; i < count; i++) {
                checkId += in.readUVarInt();
                codecs.put(checkId, in.readUVarInt());
            }
            return new Decoded(format, flavor, Collections.unmodifiableMap(codecs), true);
        } catch (RuntimeException e) {
            return new Decoded(FORMAT_VERSION, FLAVOR_UNKNOWN, Collections.<Integer, Integer>emptyMap(), false);
        }
    }

    private static void writeUVarInt(@NotNull ByteArrayOutputStream out, int value) {
        int v = value;
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    public static final class Decoded {
        private final int formatVersion;
        private final int flavor;
        private final Map<Integer, Integer> checkCodecVersions;
        private final boolean supported;

        public Decoded(int formatVersion, int flavor, @NotNull Map<Integer, Integer> checkCodecVersions, boolean supported) {
            this.formatVersion = formatVersion;
            this.flavor = flavor;
            this.checkCodecVersions = checkCodecVersions == null ? Collections.<Integer, Integer>emptyMap() : checkCodecVersions;
            this.supported = supported;
        }

        public int formatVersion() { return formatVersion; }
        public int flavor() { return flavor; }
        public Map<Integer, Integer> checkCodecVersions() { return checkCodecVersions; }
        public boolean supported() { return supported; }

        public int codecVersionOrText(int checkId) {
            Integer v = checkCodecVersions.get(checkId);
            return v == null ? 0 : v;
        }
    }

    private static final class Reader {
        private final byte[] data;
        private int offset;
        private Reader(byte[] data) { this.data = data; }
        private int readU8() {
            if (offset >= data.length) throw new IllegalArgumentException("truncated verbose manifest");
            return data[offset++] & 0xFF;
        }
        private int readUVarInt() {
            int value = 0;
            int shift = 0;
            while (shift < 35) {
                int b = readU8();
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) return value;
                shift += 7;
            }
            throw new IllegalArgumentException("verbose manifest varint is too long");
        }
    }
}
