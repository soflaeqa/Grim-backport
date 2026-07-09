package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.ViolationRecord;

import java.util.UUID;

public final class Queries {
    private Queries() {
    }

    public static ListSessionsByPlayer listSessionsByPlayer(UUID player, int pageSize, Cursor cursor) {
        return new ListSessionsByPlayer(player, pageSize, cursor);
    }

    public static GetSessionById getSessionById(UUID sessionId) {
        return new GetSessionById(sessionId);
    }

    public static ListViolationsInSession listViolationsInSession(UUID sessionId, int pageSize, Cursor cursor) {
        return new ListViolationsInSession(sessionId, pageSize, cursor);
    }

    public static GetPlayerIdentity getPlayerIdentity(UUID uuid) {
        return new GetPlayerIdentity(uuid);
    }

    public static GetPlayerIdentityByName getPlayerIdentityByName(String name) {
        return new GetPlayerIdentityByName(name);
    }

    public static ListPlayersByNamePrefix listPlayersByNamePrefix(String lowerPrefix, int limit) {
        return new ListPlayersByNamePrefix(lowerPrefix, limit);
    }

    public static GetSetting getSetting(SettingScope scope, String scopeKey, String key) {
        return new GetSetting(scope, scopeKey, key);
    }

    public static final class ListSessionsByPlayer implements Query<SessionRecord> {
        private final UUID player;
        private final int pageSize;
        private final Cursor cursor;

        public ListSessionsByPlayer(UUID player, int pageSize, Cursor cursor) {
            this.player = player;
            this.pageSize = pageSize;
            this.cursor = cursor;
        }
        public UUID player() { return player; }
        public int pageSize() { return pageSize; }
        public Cursor cursor() { return cursor; }
        public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ListSessionsByPlayer)) return false; ListSessionsByPlayer that = (ListSessionsByPlayer)o; return pageSize == that.pageSize && eq(player, that.player) && eq(cursor, that.cursor); }
        public int hashCode() { int r = hc(player); r = 31 * r + pageSize; r = 31 * r + hc(cursor); return r; }
        public String toString() { return "ListSessionsByPlayer[player=" + player + ", pageSize=" + pageSize + ", cursor=" + cursor + "]"; }
    }

    public static final class GetSessionById implements Query<SessionRecord> {
        private final UUID sessionId;
        public GetSessionById(UUID sessionId) { this.sessionId = sessionId; }
        public UUID sessionId() { return sessionId; }
        public boolean equals(Object o) { return this == o || (o instanceof GetSessionById && eq(sessionId, ((GetSessionById)o).sessionId)); }
        public int hashCode() { return hc(sessionId); }
        public String toString() { return "GetSessionById[sessionId=" + sessionId + "]"; }
    }

    public static final class ListViolationsInSession implements Query<ViolationRecord> {
        private final UUID sessionId;
        private final int pageSize;
        private final Cursor cursor;
        public ListViolationsInSession(UUID sessionId, int pageSize, Cursor cursor) { this.sessionId = sessionId; this.pageSize = pageSize; this.cursor = cursor; }
        public UUID sessionId() { return sessionId; }
        public int pageSize() { return pageSize; }
        public Cursor cursor() { return cursor; }
        public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ListViolationsInSession)) return false; ListViolationsInSession that = (ListViolationsInSession)o; return pageSize == that.pageSize && eq(sessionId, that.sessionId) && eq(cursor, that.cursor); }
        public int hashCode() { int r = hc(sessionId); r = 31 * r + pageSize; r = 31 * r + hc(cursor); return r; }
        public String toString() { return "ListViolationsInSession[sessionId=" + sessionId + ", pageSize=" + pageSize + ", cursor=" + cursor + "]"; }
    }

    public static final class GetPlayerIdentity implements Query<PlayerIdentity> {
        private final UUID uuid;
        public GetPlayerIdentity(UUID uuid) { this.uuid = uuid; }
        public UUID uuid() { return uuid; }
        public boolean equals(Object o) { return this == o || (o instanceof GetPlayerIdentity && eq(uuid, ((GetPlayerIdentity)o).uuid)); }
        public int hashCode() { return hc(uuid); }
        public String toString() { return "GetPlayerIdentity[uuid=" + uuid + "]"; }
    }

    public static final class GetPlayerIdentityByName implements Query<PlayerIdentity> {
        private final String name;
        public GetPlayerIdentityByName(String name) { this.name = name; }
        public String name() { return name; }
        public boolean equals(Object o) { return this == o || (o instanceof GetPlayerIdentityByName && eq(name, ((GetPlayerIdentityByName)o).name)); }
        public int hashCode() { return hc(name); }
        public String toString() { return "GetPlayerIdentityByName[name=" + name + "]"; }
    }

    public static final class ListPlayersByNamePrefix implements Query<PlayerIdentity> {
        private final String lowerPrefix;
        private final int limit;
        public ListPlayersByNamePrefix(String lowerPrefix, int limit) { this.lowerPrefix = lowerPrefix; this.limit = limit; }
        public String lowerPrefix() { return lowerPrefix; }
        public int limit() { return limit; }
        public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ListPlayersByNamePrefix)) return false; ListPlayersByNamePrefix that = (ListPlayersByNamePrefix)o; return limit == that.limit && eq(lowerPrefix, that.lowerPrefix); }
        public int hashCode() { return 31 * hc(lowerPrefix) + limit; }
        public String toString() { return "ListPlayersByNamePrefix[lowerPrefix=" + lowerPrefix + ", limit=" + limit + "]"; }
    }

    public static final class GetSetting implements Query<SettingRecord> {
        private final SettingScope scope;
        private final String scopeKey;
        private final String key;
        public GetSetting(SettingScope scope, String scopeKey, String key) { this.scope = scope; this.scopeKey = scopeKey; this.key = key; }
        public SettingScope scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        public String key() { return key; }
        public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetSetting)) return false; GetSetting that = (GetSetting)o; return eq(scope, that.scope) && eq(scopeKey, that.scopeKey) && eq(key, that.key); }
        public int hashCode() { int r = hc(scope); r = 31 * r + hc(scopeKey); r = 31 * r + hc(key); return r; }
        public String toString() { return "GetSetting[scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + "]"; }
    }

    private static boolean eq(Object a, Object b) { return a == null ? b == null : a.equals(b); }
    private static int hc(Object o) { return o == null ? 0 : o.hashCode(); }
}
