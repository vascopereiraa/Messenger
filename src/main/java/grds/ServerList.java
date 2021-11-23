package grds;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerList {

    private ArrayList<ServerInfo> serverList;
    private int next;

    public ServerList() throws UnknownHostException {
        serverList = new ArrayList<>(Arrays.asList(new ServerInfo(InetAddress.getByName("128.0.0.0"), 18902)));
        next = 0;
    }

    public void addServer(ServerInfo newServer){
        if(newServer != null)
            serverList.add(newServer);
    }

    public void removeServer(ServerInfo remServer){
        if(remServer != null)
            serverList.remove(remServer);
    }

    public ServerInfo getNextServer() {
        // System.out.println("Server number: " + ((++next) % serverList.size()));
        return serverList.get(((++next) % serverList.size()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var x : serverList)
            sb.append(x.getIp()).append(":").append(x.getPort());
        return sb.toString();
    }
}
