package ac.grim.grimac.api.storage.verbose;

import org.jetbrains.annotations.NotNull;

public interface VerboseFormatter {
    int version();

    void render(@NotNull VerboseBuf in, @NotNull VerboseRenderContext ctx, @NotNull VerboseSink out);
}
