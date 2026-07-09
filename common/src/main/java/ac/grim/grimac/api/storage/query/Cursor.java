package ac.grim.grimac.api.storage.query;

public final class Cursor {
    private final String token;

    public Cursor(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cursor)) return false;
        Cursor cursor = (Cursor) o;
        return token == null ? cursor.token == null : token.equals(cursor.token);
    }

    @Override
    public int hashCode() {
        return token == null ? 0 : token.hashCode();
    }

    @Override
    public String toString() {
        return "Cursor[token=" + token + "]";
    }
}
