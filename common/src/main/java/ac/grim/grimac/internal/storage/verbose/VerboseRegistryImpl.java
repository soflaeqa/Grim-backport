package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.event.VerboseSchemaEvent;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseFormatter;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseSink;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public final class VerboseRegistryImpl implements VerboseRegistry {
    private final @Nullable DataStore store;
    private final @NotNull CheckRegistry checks;
    private final int flavor;

    private final ConcurrentMap<String, VerboseSchema> schemasByStableKey = new ConcurrentHashMap<String, VerboseSchema>();
    private final ConcurrentMap<String, Verbose> templatesByStableKey = new ConcurrentHashMap<String, Verbose>();
    private final ConcurrentMap<String, VerboseFormatter> formattersByStableKey = new ConcurrentHashMap<String, VerboseFormatter>();
    private final ConcurrentMap<FormatterKey, VerboseFormatter> formattersByTuple = new ConcurrentHashMap<FormatterKey, VerboseFormatter>();
    private final ConcurrentMap<LayoutKey, VerboseSchema.Layout> layoutCache = new ConcurrentHashMap<LayoutKey, VerboseSchema.Layout>();
    private volatile @Nullable Runnable changeListener;

    public VerboseRegistryImpl(@Nullable DataStore store, @NotNull CheckRegistry checks, int flavor) {
        this.store = store;
        this.checks = checks;
        this.flavor = flavor;
    }

    public VerboseRegistryImpl(@Nullable DataStore store,
                               @NotNull CheckRegistry checks,
                               int flavor,
                               @NotNull Category<VerboseSchemaEvent> category,
                               @NotNull Logger logger) {
        this(store, checks, flavor);
    }

    @Override
    public void register(@NotNull String stableKey, @NotNull VerboseSchema schema) {
        if (stableKey.length() == 0) throw new IllegalArgumentException("stableKey");
        schemasByStableKey.put(stableKey, schema);
        Optional<Integer> id = checks.getId(stableKey);
        if (id.isPresent()) {
            layoutCache.put(new LayoutKey(flavor, id.get().intValue(), schema.version()), new VerboseSchema.Layout(schema.fields()));
        }
    }

    @Override
    public void registerFormatter(@NotNull String stableKey, @NotNull VerboseFormatter formatter) {
        if (stableKey.length() == 0) throw new IllegalArgumentException("stableKey");
        formattersByStableKey.put(stableKey, formatter);
        Optional<Integer> id = checks.getId(stableKey);
        if (id.isPresent()) {
            formattersByTuple.put(new FormatterKey(flavor, id.get().intValue(), formatter.version()), formatter);
        }
    }

    @Override
    public void registerTemplate(@NotNull String stableKey,
                                 @NotNull String checkName,
                                 @Nullable String description,
                                 @Nullable String pluginVersion,
                                 @NotNull Verbose verbose) {
        if (stableKey.length() == 0) throw new IllegalArgumentException("stableKey");
        templatesByStableKey.put(stableKey, verbose);
        schemasByStableKey.put(stableKey, verbose.schema());
        VerboseFormatter formatter = verbose.asFormatter();
        formattersByStableKey.put(stableKey, formatter);
        int checkId = checks.intern(stableKey, checkName, description, pluginVersion);
        layoutCache.put(new LayoutKey(flavor, checkId, verbose.version()), new VerboseSchema.Layout(verbose.schema().fields()));
        formattersByTuple.put(new FormatterKey(flavor, checkId, verbose.version()), formatter);
        Runnable listener = changeListener;
        if (listener != null) listener.run();
    }

    @Override
    public void onChange(@Nullable Runnable listener) {
        this.changeListener = listener;
    }

    @Override
    public @NotNull String render(@NotNull String stableKey, byte @NotNull [] data, @NotNull VerboseRenderContext ctx) {
        if (stableKey.length() == 0) return "";
        try {
            VerboseFormatter formatter = formattersByStableKey.get(stableKey);
            if (formatter != null) {
                StringBuilder out = new StringBuilder();
                formatter.render(VerboseBuf.wrap(data == null ? new byte[0] : data), ctx, VerboseSink.into(out));
                return out.toString();
            }
            Verbose verbose = templatesByStableKey.get(stableKey);
            if (verbose != null) {
                return verbose.render(data == null ? new byte[0] : data, ctx);
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    @Override
    public @NotNull Map<Integer, Integer> checkIdVersions(@NotNull CheckRegistry checks) {
        Map<Integer, Integer> versions = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<String, VerboseSchema> entry : schemasByStableKey.entrySet()) {
            Optional<Integer> id = checks.getId(entry.getKey());
            if (id.isPresent()) versions.put(id.get(), entry.getValue().version());
        }
        return Collections.unmodifiableMap(versions);
    }

    @Override
    public @Nullable VerboseFormatter codeFormatter(int flavor, int checkId, int version) {
        if (flavor != this.flavor) return null;
        return formattersByTuple.get(new FormatterKey(flavor, checkId, version));
    }

    @Override
    public @Nullable VerboseSchema.Layout layout(int flavor, int checkId, int version) {
        return layoutCache.get(new LayoutKey(flavor, checkId, version));
    }

    @SuppressWarnings("unused")
    private DataStore store() {
        return store;
    }

    private static final class LayoutKey {
        private final int flavor;
        private final int checkId;
        private final int version;
        private LayoutKey(int flavor, int checkId, int version) {
            this.flavor = flavor; this.checkId = checkId; this.version = version;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LayoutKey)) return false;
            LayoutKey that = (LayoutKey) o;
            return flavor == that.flavor && checkId == that.checkId && version == that.version;
        }
        @Override public int hashCode() {
            int result = flavor;
            result = 31 * result + checkId;
            result = 31 * result + version;
            return result;
        }
    }

    private static final class FormatterKey {
        private final int flavor;
        private final int checkId;
        private final int version;
        private FormatterKey(int flavor, int checkId, int version) {
            this.flavor = flavor; this.checkId = checkId; this.version = version;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FormatterKey)) return false;
            FormatterKey that = (FormatterKey) o;
            return flavor == that.flavor && checkId == that.checkId && version == that.version;
        }
        @Override public int hashCode() {
            int result = flavor;
            result = 31 * result + checkId;
            result = 31 * result + version;
            return result;
        }
    }
}
