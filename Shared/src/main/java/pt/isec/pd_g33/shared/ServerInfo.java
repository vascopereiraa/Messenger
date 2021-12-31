package pt.isec.pd_g33.shared;

import java.net.InetAddress;
import java.util.Objects;

public class ServerInfo {

    // Server location
    private final InetAddress ip;
    private final int port;

    // Hearthbeat markers
    private boolean isAlive;
    private int hearthbeatFail;

    // new Server
    boolean newServer = true;

    public void setNewServer(boolean newServer) {
        this.newServer = newServer;
    }

    public boolean isNewServer() {
        if(newServer){
            newServer = false;
            return true;
        }
        return newServer;
    }

    public ServerInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        this.isAlive = true;
        this.hearthbeatFail = 0;
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

    public int getHearthbeatFail() {
        return hearthbeatFail;
    }

    public void resetHearthbeatFail() {
        hearthbeatFail = 0;
    }

    public void incHearthbeatFail() {
        hearthbeatFail++;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", ip.getHostName(), port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInfo that = (ServerInfo) o;

        if (port != that.port) return false;
        return Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
