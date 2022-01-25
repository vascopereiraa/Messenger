package pt.isec.pd_g33.grds.data;

import pt.isec.pd_g33.grds.coms.ThreadNotificationMulticast;
import pt.isec.pd_g33.shared.ServerInfo;

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
        // Caso ja exista, atualiza o seu indicador que esta vivo
        if(serverList.contains(newServer)) {
            /*if(!serverList.get(serverList.indexOf(newServer)).getHearthbeat()){
                ThreadNotificationMulticast.synchronizeFiles();
            }*/
            serverList.get(serverList.indexOf(newServer)).setDate(System.currentTimeMillis());
            serverList.get(serverList.indexOf(newServer)).markAsAlive();
            serverList.get(serverList.indexOf(newServer)).setNewServer(false);
            // System.out.println("O servidor já estava registado! -> Hearthbeat a zeros");
            return false;
        }
        // System.out.println("Novo servidor registado");
        newServer.setDate(System.currentTimeMillis());
        serverList.add(newServer);
        newServer.markAsAlive();
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

    public ServerInfo getServerInfoByPorto(int porto){
        for(ServerInfo server : serverList)
            if(server.getPort() == porto)
                return server;
        return null;
    }

    public int getNextIndex() { return index++;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Online Servers:\n");
        ServerInfo server;
        for (int i = 0; i < serverList.size(); ++i) {
            server = serverList.get(i);
            if(server.getHearthbeat()){
                sb.append("Server ").append(String.format("%04d", i)).append(": ").append(server)
                        .append("\tTime since heartbeat: ")
                        .append((System.currentTimeMillis() - server.getDate()) / 1000).append(" seconds")
                        .append("\n");
            }
        }
        return sb.toString();
    }
}
