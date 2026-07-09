package ac.grim.grimac.api.storage.verbose;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Verbose {
    private final List<String> templates;

    private Verbose(List<String> templates) {
        this.templates = Collections.unmodifiableList(new ArrayList<String>(templates));
    }

    public static Verbose of(String template) {
        List<String> list = new ArrayList<String>();
        list.add(template == null ? "" : template);
        return new Verbose(list);
    }

    public Verbose or(String template) {
        List<String> list = new ArrayList<String>(templates);
        list.add(template == null ? "" : template);
        return new Verbose(list);
    }

    public String template() {
        return templates.isEmpty() ? "" : templates.get(0);
    }

    public List<String> templates() {
        return templates;
    }

    public int version() {
        return 1;
    }

    public byte[] layoutBytes() {
        return schema().layoutBytes();
    }

    public VerboseFormatter asFormatter() {
        final Verbose self = this;
        return new VerboseFormatter() {
            @Override
            public int version() {
                return self.version();
            }

            @Override
            public void render(VerboseBuf in, VerboseRenderContext ctx, VerboseSink out) {
                out.text(self.render(in == null ? new byte[0] : in.toByteArray(), ctx));
            }
        };
    }

    public VerboseSchema schema() {
        return new VerboseSchema(Collections.<VerboseSchema.TypeTag>emptyList());
    }

    public Writer write(VerboseBuf buf) {
        if (buf == null) buf = new VerboseBuf();
        return new Writer(this, buf.reset());
    }

    /**
     * Java 8-compatible replacement for the original overload that selects one
     * of the alternative templates created via {@link #or(String)}.
     */
    public Writer write(VerboseBuf buf, int templateIndex) {
        if (buf == null) buf = new VerboseBuf();
        return new Writer(selectTemplate(templateIndex), buf.reset());
    }

    private Verbose selectTemplate(int templateIndex) {
        if (templates.isEmpty()) {
            return this;
        }
        int index = templateIndex;
        if (index < 0 || index >= templates.size()) {
            index = 0;
        }
        List<String> selected = new ArrayList<String>(1);
        selected.add(templates.get(index));
        return new Verbose(selected);
    }

    public String render(byte[] data, VerboseRenderContext context) {
        String template = template();
        VerboseReader in = new VerboseReader(data);
        StringBuilder out = new StringBuilder(template.length() + 32);
        int index = 0;
        while (index < template.length()) {
            int open = template.indexOf('{', index);
            if (open < 0) {
                out.append(template.substring(index));
                break;
            }
            out.append(template.substring(index, open));
            int close = template.indexOf('}', open + 1);
            if (close < 0) {
                out.append(template.substring(open));
                break;
            }
            String token = template.substring(open + 1, close);
            renderToken(token, in, context, out);
            index = close + 1;
        }
        return out.toString();
    }

    private static void renderToken(String token, VerboseReader in, VerboseRenderContext context, StringBuilder out) {
        String name = token;
        String fmt = null;
        int colon = token.indexOf(':');
        if (colon >= 0) {
            name = token.substring(0, colon);
            fmt = token.substring(colon + 1);
        }

        VerboseTags.RegisteredTag registered = VerboseTags.get(name);
        if (registered != null) {
            registered.render(in, context, out, fmt);
            return;
        }

        if ("f64".equals(name)) {
            appendFormatted(out, fmt, in.rf64());
        } else if ("f32".equals(name)) {
            appendFormatted(out, fmt, in.rf32());
        } else if ("uint".equals(name) || "sint".equals(name)) {
            out.append(in.rzz());
        } else if ("ulong".equals(name) || "slong".equals(name)) {
            out.append(in.rvl());
        } else if ("bool".equals(name)) {
            out.append(in.rbool());
        } else if ("str".equals(name)) {
            out.append(in.rstr());
        } else if ("mcpos".equals(name)) {
            int x = in.rzz();
            int y = in.rzz();
            int z = in.rzz();
            out.append(x).append(',').append(' ').append(y).append(',').append(' ').append(z);
        } else if ("cursor".equals(name)) {
            float x = in.rf32();
            float y = in.rf32();
            float z = in.rf32();
            out.append(x).append(',').append(' ').append(y).append(',').append(' ').append(z);
        } else {
            out.append('{').append(token).append('}');
        }
    }

    private static void appendFormatted(StringBuilder out, String fmt, double value) {
        if (fmt != null && fmt.length() > 0) {
            try {
                out.append(String.format(fmt, value));
                return;
            } catch (RuntimeException ignored) {
            }
        }
        out.append(value);
    }

    public static List<Verbose> declaredBy(Class<?> start, Class<?> stopBefore) {
        List<Verbose> result = new ArrayList<Verbose>();
        Class<?> type = start;
        while (type != null && type != stopBefore) {
            Field[] fields = type.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (!Verbose.class.equals(field.getType())) continue;
                if (!Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof Verbose) {
                        result.add((Verbose) value);
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
        return result;
    }

    public static final class Writer {
        private final Verbose verbose;
        private final VerboseBuf buf;

        private Writer(Verbose verbose, VerboseBuf buf) {
            this.verbose = verbose;
            this.buf = buf;
        }

        public Verbose verbose() {
            return verbose;
        }

        public VerboseBuf end() {
            return buf;
        }

        public Writer f64(double value) {
            buf.writeDouble(value);
            return this;
        }

        public Writer f32(float value) {
            buf.writeFloat(value);
            return this;
        }

        public Writer uint(int value) {
            buf.writeInt(value);
            return this;
        }

        public Writer sint(int value) {
            buf.writeInt(value);
            return this;
        }

        public Writer ulong(long value) {
            buf.writeLong(value);
            return this;
        }

        public Writer slong(long value) {
            buf.writeLong(value);
            return this;
        }

        public Writer bool(boolean value) {
            buf.writeBoolean(value);
            return this;
        }

        public Writer str(String value) {
            buf.writeString(value);
            return this;
        }

        public Writer mcPos(int x, int y, int z) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
            return this;
        }

        public Writer cursor(float x, float y, float z) {
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            return this;
        }
    }
}
