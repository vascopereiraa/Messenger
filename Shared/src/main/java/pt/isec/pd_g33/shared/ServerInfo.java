package pt.isec.pd_g33.shared;

import java.net.InetAddress;

public class ServerInfo {

    private InetAddress ip;
    private int port;

    private boolean isAlive;

    public ServerInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        this.isAlive = true;
    }

    // Server info
    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    // Hearthbeat management methods
    public boolean getHearthbeat() {
        return isAlive;
    }

    public void markAsDead() {
        isAlive = false;
    }

    public void markAsAlive() {
        isAlive = true;
    }

    @Override
    public String toString() {
        return String.format("Server: %s:%d", ip.getHostName(), port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        if (port != that.port) return false;
        return ip != null ? ip.equals(that.ip) : that.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
