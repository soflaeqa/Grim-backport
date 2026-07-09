package ac.grim.grimac.api.storage.verbose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VerboseTags {
    private static final Map<String, RegisteredTag> TAGS = new HashMap<String, RegisteredTag>();

    private VerboseTags() {
    }

    public interface Renderer {
        void render(VerboseReader in, VerboseRenderContext ctx, StringBuilder out, String fmt);
    }

    static RegisteredTag get(String name) {
        return TAGS.get(name);
    }

    public static void register(String name, List<VerboseSchema.TypeTag> wire, Renderer renderer) {
        if (name == null || renderer == null) return;
        TAGS.put(name, new RegisteredTag(wire, renderer));
    }

    public static void registerEnum(String name, Enum<?>[] values) {
        registerEnum0(name, values, false);
    }

    public static void registerEnumLower(String name, Enum<?>[] values) {
        registerEnum0(name, values, true);
    }

    private static void registerEnum0(String name, final Enum<?>[] values, final boolean lower) {
        List<VerboseSchema.TypeTag> wire = Collections.singletonList(VerboseSchema.TypeTag.VI);
        register(name, wire, new Renderer() {
            @Override
            public void render(VerboseReader in, VerboseRenderContext ctx, StringBuilder out, String fmt) {
                int id = in.rvi();
                if (values == null || id < 0 || id >= values.length || values[id] == null) {
                    out.append("unknown");
                    return;
                }
                String value = values[id].name();
                out.append(lower ? value.toLowerCase(Locale.ROOT) : value);
            }
        });
    }

    public static int enumId(Enum<?> value) {
        return value == null ? -1 : value.ordinal();
    }

    public static final class RegisteredTag {
        private final List<VerboseSchema.TypeTag> wire;
        private final Renderer renderer;

        private RegisteredTag(List<VerboseSchema.TypeTag> wire, Renderer renderer) {
            this.wire = wire == null ? Collections.<VerboseSchema.TypeTag>emptyList() : Collections.unmodifiableList(new ArrayList<VerboseSchema.TypeTag>(wire));
            this.renderer = renderer;
        }

        public List<VerboseSchema.TypeTag> wire() {
            return wire;
        }

        public List<VerboseSchema.TypeTag> schema() {
            return wire;
        }

        void render(VerboseReader in, VerboseRenderContext ctx, StringBuilder out, String fmt) {
            renderer.render(in, ctx, out, fmt);
        }
    }
}
