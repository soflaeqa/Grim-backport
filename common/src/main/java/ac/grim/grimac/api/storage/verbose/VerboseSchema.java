package ac.grim.grimac.api.storage.verbose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VerboseSchema {
    private final List<TypeTag> tags;

    public VerboseSchema(List<TypeTag> tags) {
        this.tags = tags == null ? Collections.<TypeTag>emptyList() : Collections.unmodifiableList(new ArrayList<TypeTag>(tags));
    }

    public List<TypeTag> tags() {
        return tags;
    }

    public List<TypeTag> typeTags() {
        return tags;
    }

    public List<Field> fields() {
        List<Field> fields = new ArrayList<Field>(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            fields.add(new Field("field" + i, tags.get(i)));
        }
        return Collections.unmodifiableList(fields);
    }

    public int version() {
        return 1;
    }

    public byte[] layoutBytes() {
        byte[] out = new byte[tags.size()];
        for (int i = 0; i < tags.size(); i++) out[i] = (byte) tags.get(i).tag();
        return out;
    }

    public static Layout decodeLayout(byte[] bytes) {
        List<Field> fields = new ArrayList<Field>();
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                fields.add(new Field("field" + i, TypeTag.fromTag(bytes[i] & 0xFF)));
            }
        }
        return new Layout(fields);
    }

    public static final class Layout {
        private final List<Field> fields;

        public Layout(List<Field> fields) {
            this.fields = fields == null ? Collections.<Field>emptyList() : Collections.unmodifiableList(new ArrayList<Field>(fields));
        }

        public List<Field> fields() {
            return fields;
        }
    }

    public static final class Field {
        private final String name;
        private final TypeTag type;

        public Field(String name, TypeTag type) {
            this.name = name == null ? "" : name;
            this.type = type == null ? TypeTag.STR : type;
        }

        public String name() {
            return name;
        }

        public TypeTag type() {
            return type;
        }
    }

    public enum TypeTag {
        F64('d'),
        F32('f'),
        VI('v'),
        ZZ('z'),
        VL('l'),
        BOOL('b'),
        STR('s'),
        ENUM('e');

        private final int tag;

        TypeTag(int tag) {
            this.tag = tag;
        }

        public int tag() {
            return tag;
        }

        public static TypeTag fromTag(int tag) {
            for (TypeTag value : values()) {
                if (value.tag == tag) return value;
            }
            return STR;
        }
    }
}
