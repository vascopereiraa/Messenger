package data;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;

public class ConnectionMessage implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private InetAddress ip;
    private int port;
    private String message;

    public ConnectionMessage() {}

    public ConnectionMessage(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
