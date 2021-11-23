package grds;

import data.ConnectionMessage;
import data.ConnectionType;

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

                if(connectionMessage.getConnectionType() == ConnectionType.Server) {
                    serverList.addServer(new ServerInfo(dp.getAddress(),dp.getPort()));
                    connectionMessage.setMessage("Server Registered");
                    System.out.println("Novo servidor registado");
                    System.out.println(serverList);
                }else{
                    connectionMessage.insertServerInfo(serverList.getNextServer());
                    System.out.println("Connection info: " + connectionMessage.getIp() + " : " + connectionMessage.getPort());
                }
                // Envio de mensagem de volta ao cliente(informa de server a conectar) e servidor
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(connectionMessage);
                out.flush();

                dp.setData(baos.toByteArray());
                dp.setLength(baos.toByteArray().length);
                ds.send(dp);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
