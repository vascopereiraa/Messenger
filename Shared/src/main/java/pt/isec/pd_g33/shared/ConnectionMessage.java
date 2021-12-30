package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;

public class ConnectionMessage implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private InetAddress ip;
    private int port;
    private String message;
    private final ConnectionType connectionType;

    // Client Constructor
    public ConnectionMessage(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    // Server Constructor
    public ConnectionMessage(int port, ConnectionType connectionType) {
        this.port = port;
        this.connectionType = connectionType;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "ConnectionMessage{" +
                "ip=" + ip +
                ", port=" + port +
                ", message='" + message + '\'' +
                ", connectionType=" + connectionType +
                '}';
    }

    public void insertServerInfo(ServerInfo serverInfo) {
        ip = serverInfo.getIp();
        port = serverInfo.getPort();
    }
}
