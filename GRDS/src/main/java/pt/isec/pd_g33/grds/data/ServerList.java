package pt.isec.pd_g33.grds.data;

import pt.isec.pd_g33.grds.coms.ThreadNotificationMulticast;
import pt.isec.pd_g33.shared.ServerInfo;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerList {

    private final CopyOnWriteArrayList<ServerInfo> serverList;
    private int next;
    private int index;

    public ServerList() {
        serverList = new CopyOnWriteArrayList<>();
        next = 0;
        index = 0;
    }

    public boolean addServer(ServerInfo newServer){
        if(serverList.contains(newServer)) {
            if(!serverList.get(serverList.indexOf(newServer)).getHearthbeat())
                ThreadNotificationMulticast.synchronizeFiles();
            serverList.get(serverList.indexOf(newServer)).markAsAlive();
            serverList.get(serverList.indexOf(newServer)).setNewServer(false);
            // System.out.println("O servidor já estava registado! -> Hearthbeat a zeros");
            return false;
        }
        // System.out.println("Novo servidor registado");
        serverList.add(newServer);
        return true;
    }

    public ServerInfo getNextServer() {
        if(serverList.size() == 0)
            return new ServerInfo(null, 0);

        ServerInfo aux;
        do {
            aux = serverList.get(((++next) % serverList.size()));
        } while(aux.getHearthbeatFail() != 0);

        // System.out.println("Server number: " + ((++next) % serverList.size()));
        return aux;
    }

    public CopyOnWriteArrayList<ServerInfo> getServerInfo(){
        return serverList;
    }

    public int getNextIndex() { return index++;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Online Servers:\n");
        ServerInfo server;
        for (int i = 0; i < serverList.size(); ++i) {
            server = serverList.get(i);
            if(server.getHearthbeat())
                sb.append("Server ").append(String.format("%04d", i)).append(": ").append(server).append("\n");
        }
        return sb.toString();
    }
}
