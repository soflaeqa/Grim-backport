package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.api.storage.backend.BackendConfigSource;

import java.util.Objects;

public final class TableNames {
    public static final String DEFAULT_META = "grim_meta";
    public static final String DEFAULT_CHECKS = "grim_checks";
    public static final String DEFAULT_PLAYERS = "grim_players";
    public static final String DEFAULT_SESSIONS = "grim_sessions";
    public static final String DEFAULT_VIOLATIONS = "grim_violations";
    public static final String DEFAULT_SETTINGS = "grim_settings";

    public static final TableNames DEFAULTS = new TableNames(
            DEFAULT_META,
            DEFAULT_CHECKS,
            DEFAULT_PLAYERS,
            DEFAULT_SESSIONS,
            DEFAULT_VIOLATIONS,
            DEFAULT_SETTINGS);

    private final String meta;
    private final String checks;
    private final String players;
    private final String sessions;
    private final String violations;
    private final String settings;

    public TableNames(String meta, String checks, String players, String sessions, String violations, String settings) {
        this.meta = isBlank(meta) ? DEFAULT_META : meta;
        this.checks = isBlank(checks) ? DEFAULT_CHECKS : checks;
        this.players = isBlank(players) ? DEFAULT_PLAYERS : players;
        this.sessions = isBlank(sessions) ? DEFAULT_SESSIONS : sessions;
        this.violations = isBlank(violations) ? DEFAULT_VIOLATIONS : violations;
        this.settings = isBlank(settings) ? DEFAULT_SETTINGS : settings;
    }

    public static TableNames readFrom(BackendConfigSource source) {
        return new TableNames(
                source.getString("tables.meta", DEFAULT_META),
                source.getString("tables.checks", DEFAULT_CHECKS),
                source.getString("tables.players", DEFAULT_PLAYERS),
                source.getString("tables.sessions", DEFAULT_SESSIONS),
                source.getString("tables.violations", DEFAULT_VIOLATIONS),
                source.getString("tables.settings", DEFAULT_SETTINGS));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String meta() {
        return meta;
    }

    public String checks() {
        return checks;
    }

    public String players() {
        return players;
    }

    public String sessions() {
        return sessions;
    }

    public String violations() {
        return violations;
    }

    public String settings() {
        return settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableNames)) return false;
        TableNames that = (TableNames) o;
        return Objects.equals(meta, that.meta)
                && Objects.equals(checks, that.checks)
                && Objects.equals(players, that.players)
                && Objects.equals(sessions, that.sessions)
                && Objects.equals(violations, that.violations)
                && Objects.equals(settings, that.settings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meta, checks, players, sessions, violations, settings);
    }

    @Override
    public String toString() {
        return "TableNames[meta=" + meta
                + ", checks=" + checks
                + ", players=" + players
                + ", sessions=" + sessions
                + ", violations=" + violations
                + ", settings=" + settings + "]";
    }
}
