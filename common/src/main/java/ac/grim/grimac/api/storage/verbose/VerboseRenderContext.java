package ac.grim.grimac.api.storage.verbose;

public final class VerboseRenderContext {
    private final int clientVersionPvn;
    private final String serverVersionString;

    public VerboseRenderContext(int clientVersionPvn, String serverVersionString) {
        this.clientVersionPvn = clientVersionPvn;
        this.serverVersionString = serverVersionString;
    }

    public int clientVersionPvn() {
        return clientVersionPvn;
    }

    public String serverVersionString() {
        return serverVersionString;
    }
}
