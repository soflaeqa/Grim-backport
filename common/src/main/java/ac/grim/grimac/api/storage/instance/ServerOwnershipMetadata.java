package ac.grim.grimac.api.storage.instance;

import java.util.Objects;

public final class ServerOwnershipMetadata {
    private final String serverName;
    private final String hostname;
    private final String grimVersion;
    private final String serverVersionString;

    public ServerOwnershipMetadata(String serverName, String hostname, String grimVersion, String serverVersionString) {
        this.serverName = serverName;
        this.hostname = hostname;
        this.grimVersion = grimVersion;
        this.serverVersionString = serverVersionString;
    }

    public String serverName() { return serverName; }
    public String hostname() { return hostname; }
    public String grimVersion() { return grimVersion; }
    public String serverVersionString() { return serverVersionString; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerOwnershipMetadata)) return false;
        ServerOwnershipMetadata that = (ServerOwnershipMetadata) o;
        return Objects.equals(serverName, that.serverName)
                && Objects.equals(hostname, that.hostname)
                && Objects.equals(grimVersion, that.grimVersion)
                && Objects.equals(serverVersionString, that.serverVersionString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, hostname, grimVersion, serverVersionString);
    }

    @Override
    public String toString() {
        return "ServerOwnershipMetadata[serverName=" + serverName
                + ", hostname=" + hostname
                + ", grimVersion=" + grimVersion
                + ", serverVersionString=" + serverVersionString + ']';
    }
}
