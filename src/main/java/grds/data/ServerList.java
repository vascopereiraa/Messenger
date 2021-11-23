package grds.data;

import java.util.ArrayList;

public class ServerList {

    private ArrayList<ServerInfo> serverList;
    private int next;

    public ServerList() {
        serverList = new ArrayList<>();
        next = 0;
    }

    public boolean addServer(ServerInfo newServer){
        if(newServer == null) return false;
        if(serverList.contains(newServer)) {
            serverList.get(serverList.indexOf(newServer)).markAsAlive();
            return true;
        }
        serverList.add(newServer);
        return true;
    }

    public void removeServer(ServerInfo remServer){
        if(remServer != null)
            serverList.remove(remServer);
    }

    public ServerInfo getNextServer() {
        if(serverList.size() == 0)
            return new ServerInfo(null, 0);
        // System.out.println("Server number: " + ((++next) % serverList.size()));
        return serverList.get(((++next) % serverList.size()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var x : serverList)
            sb.append(x);
        return sb.toString();
    }
}
