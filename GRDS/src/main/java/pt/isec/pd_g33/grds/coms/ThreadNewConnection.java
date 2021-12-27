package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;
import pt.isec.pd_g33.shared.Notification;
import pt.isec.pd_g33.shared.ServerInfo;

import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class ThreadNewConnection implements Runnable {

    private final DatagramSocket ds;
    private final ServerList serverList;
    private ArrayList<Notification> filesReceived;

    public ThreadNewConnection(DatagramSocket ds, ServerList serverList,ArrayList<Notification> filesReceived) {
        this.ds = ds;
        this.serverList = serverList;
        this.filesReceived = filesReceived;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while(true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);

                System.out.println("Received");

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                ConnectionMessage connectionMessage = (ConnectionMessage) ois.readObject();

                // Message Processing
                if(connectionMessage.getConnectionType() == ConnectionType.Server) {
                    if(serverList.addServer(new ServerInfo(dp.getAddress(), connectionMessage.getPort()))) {
                        connectionMessage.setMessage("Server_%04d".formatted(serverList.getNextIndex()));
                        // System.out.println("New server info: " + dp.getAddress().getHostAddress() + " : " + connectionMessage.getPort());
                        System.out.println(serverList);
                    }
                }
                else {
                    connectionMessage.insertServerInfo(serverList.getNextServer());
                    // System.out.println("New client info: " + connectionMessage.getIp() + " : " + connectionMessage.getPort());
                }

                // Envio de mensagem de volta ao cliente(informa de server a conectar) e servidor
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(connectionMessage);
                out.flush();

                dp.setData(baos.toByteArray());
                dp.setLength(baos.toByteArray().length);
                ds.send(dp);

                if(connectionMessage.getConnectionType() == ConnectionType.Server &&
                    serverList.getServerInfo().get(serverList.getServerInfo().size() - 1).isNewServer() == true){
                    // Obtenção de todos os ficheiros existentes
                    ThreadNotificationMulticast.synchronizeFiles();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
