package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseSink;
import org.jetbrains.annotations.NotNull;

public final class GenericVerboseReader {
    private GenericVerboseReader() {}

    public static void render(@NotNull VerboseSchema.Layout layout,
                              @NotNull VerboseBuf in,
                              @NotNull VerboseRenderContext ctx,
                              @NotNull VerboseSink out) throws UnderflowException {
        try {
            for (VerboseSchema.Field field : layout.fields()) {
                renderField(field, in, out);
            }
        } catch (VerboseBuf.UnderflowException | IllegalArgumentException e) {
            throw new UnderflowException(e);
        }
    }

    private static void renderField(@NotNull VerboseSchema.Field field, @NotNull VerboseBuf in, @NotNull VerboseSink out) {
        out.key(field.name());
        switch (field.type()) {
            case F64:
                require(in, 8);
                out.num(in.rf64());
                break;
            case F32:
                require(in, 4);
                out.num(in.rf32());
                break;
            case VI:
            case ENUM:
                require(in, 1);
                out.num(in.rvi());
                break;
            case ZZ:
                require(in, 1);
                out.num(in.rzz());
                break;
            case VL:
                require(in, 1);
                out.num(in.rvl());
                break;
            case BOOL:
                require(in, 1);
                out.bool(in.rbool());
                break;
            case STR:
            default:
                require(in, 1);
                out.text(in.rstr());
                break;
        }
    }

    private static void require(@NotNull VerboseBuf in, int bytes) {
        if (in.remaining() < bytes) throw new VerboseBuf.UnderflowException("verbose payload truncated");
    }

    public static final class UnderflowException extends Exception {
        public UnderflowException(@NotNull Throwable cause) {
            super("verbose payload truncated", cause);
        }
    }
}
