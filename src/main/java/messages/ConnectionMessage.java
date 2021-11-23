package messages;

import grds.data.ServerInfo;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;

public class ConnectionMessage implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private InetAddress ip;
    private int port;
    private String message;
    private ConnectionType connectionType;

    public ConnectionMessage(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public ConnectionMessage(InetAddress ip, int port, ConnectionType connectionType) {
        this.ip = ip;
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

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    
    public void insertServerInfo(ServerInfo serverInfo) {
        ip = serverInfo.getIp();
        port = serverInfo.getPort();
    }
}
