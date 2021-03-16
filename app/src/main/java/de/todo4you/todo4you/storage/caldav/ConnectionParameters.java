package de.todo4you.todo4you.storage.caldav;

public class ConnectionParameters {
    private final ServerType serverType;
    private final String host;
    private final int port;
    private final HttpProtocol httpProtocol;
    private final String path;
    private final String user;
    private final String password;

    /**
     * ServerType is used to determine the CalendarConnector implementation. It is also useful for
     * auto-probing connection parameters, especially the path.
     */
    public enum ServerType { GenericCaldav, Owncloud8, Owncloud9};

    public enum HttpProtocol { HTTPS(true), HTTP(false);

        private final boolean isEncrypted;

        HttpProtocol(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
        }

        public boolean isEncrypted() {
            return isEncrypted;
        }

        public String protocol() {
            return name().toLowerCase();
        }
    }

    public ConnectionParameters(ServerType serverType, String host, int port, HttpProtocol httpProtocol, String path, String user, String password) {
        this.serverType = serverType;
        this.host = host;
        this.port = port;
        this.httpProtocol = httpProtocol;
        this.path = path;
        this.user = user;
        this.password = password;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public HttpProtocol httpProtocol() {
        return httpProtocol;
    }

    public String path() {
        return path;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }
}
