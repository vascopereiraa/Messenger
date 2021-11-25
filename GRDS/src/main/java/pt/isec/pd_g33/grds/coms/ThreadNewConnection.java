package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;
import pt.isec.pd_g33.shared.ServerInfo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadNewConnection implements Runnable {

    private DatagramSocket ds;
    private ServerList serverList;

    public ThreadNewConnection(DatagramSocket ds, ServerList serverList) {
        this.ds = ds;
        this.serverList = serverList;
    }

    @Override
    public void run() {
        System.err.println("Thread a arrancar!");

        while(true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                ConnectionMessage connectionMessage = (ConnectionMessage) ois.readObject();
                System.out.println("GRDS received: " + connectionMessage.getIp() + ":" + connectionMessage.getPort());

                // Message Processing
                if(connectionMessage.getConnectionType() == ConnectionType.Server) {
                    //todo: verificar se ja existe o servidor dentro do addServer
                    serverList.addServer(new ServerInfo(connectionMessage.getIp(),connectionMessage.getPort()));
                    connectionMessage.setMessage("Server Registered");
                    System.out.println("New server info: " + connectionMessage.getIp() + " : " + connectionMessage.getPort());
                    System.out.println(serverList);
                }
                else {
                    connectionMessage.insertServerInfo(serverList.getNextServer());
                    System.out.println("New client info: " + connectionMessage.getIp() + " : " + connectionMessage.getPort());
                }

                // Envio de mensagem de volta ao cliente(informa de server a conectar) e servidor
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(connectionMessage);
                out.flush();

                dp.setData(baos.toByteArray());
                dp.setLength(baos.toByteArray().length);
                ds.send(dp);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
