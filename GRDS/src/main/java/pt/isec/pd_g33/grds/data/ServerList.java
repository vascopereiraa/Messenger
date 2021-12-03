package pt.isec.pd_g33.grds.data;

import pt.isec.pd_g33.shared.ServerInfo;

import java.util.ArrayList;

public class ServerList {

    private final ArrayList<ServerInfo> serverList;
    private int next;

    public ServerList() {
        serverList = new ArrayList<>();
        next = 0;
    }

    public boolean addServer(ServerInfo newServer){
        if(serverList.contains(newServer)) {
            serverList.get(serverList.indexOf(newServer)).markAsAlive();
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

    public ArrayList<ServerInfo> getServerInfo(){
        return serverList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Online Servers:\n");
        ServerInfo server;
        for (int i = 0; i < serverList.size(); ++i) {
            server = serverList.get(i);
            if(server.getHearthbeat())
                sb.append("Server ").append(String.format("%4d", i)).append(": ").append(server).append("\n");
        }
        return sb.toString();
    }
}
