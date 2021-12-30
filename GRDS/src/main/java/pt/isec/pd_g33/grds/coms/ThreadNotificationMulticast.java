package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Notification;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadNotificationMulticast implements Runnable {

    private static final int UNICAST_SEND_NOTIFICATION_PORT = 1000;
    private static final int UNICAST_RECEIVE_NOTIFICATION_PORT = 2000;
    private static final String UNICAST_RECEIVE_NOTIFICATION_IP = "255.255.255.255";
    private static CopyOnWriteArrayList<Notification> filesReceived = new CopyOnWriteArrayList<>();;

    private static DatagramSocket ds;
    private static MulticastSocket multicastSocket;
    private static ServerList serverList;

    public ThreadNotificationMulticast(ServerList serverList) {
        this.serverList = serverList;
    }

    @Override
    public void run() {
            try{
                ds = new DatagramSocket(UNICAST_RECEIVE_NOTIFICATION_PORT);
                multicastSocket = new MulticastSocket(UNICAST_SEND_NOTIFICATION_PORT);
                multicastSocket.setBroadcast(true);

                while(true) {
                    DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                    ds.receive(dp);

                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Notification notification = (Notification) ois.readObject();
                    System.out.println("GRDS received notification: " + notification.getDataType());

                    // Caso a notificacao seja de ficheiros
                    if(notification.getDataType() == DataType.File)
                        if(notification.getToUsername().equals("deletefile"))// Notificacao para eliminar o ficheiro, remove da lista; getContent tem nome ficheiro quando é send
                            filesReceived.removeIf(obj -> obj.getContent().equals(notification.getFromUsername())); //getFromUsername tem nome do ficheiro quando é delete
                        else // Notificacao para adicionar ficheiro, adiciona a lista
                            filesReceived.add(notification);

                    // Apagar servidor da lista de servers, caso ele faça EXIT.
                    if(notification.getContent().equals("serverTerminated") && notification.getDataType() == DataType.Message){
                        System.out.println("O servidor " + notification.getPorto() + " foi eliminado");
                        serverList.getServerInfo().removeIf(obj -> obj.getPort() == obj.getPort());
                    }

                    // Envio em multicast para todos os servidores
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(baos);
                    out.writeUnshared(notification);
                    out.flush();

                    dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,InetAddress.getByName(UNICAST_RECEIVE_NOTIFICATION_IP),UNICAST_SEND_NOTIFICATION_PORT);
                    multicastSocket.send(dp);
                }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void synchronizeFiles() {
        for (Notification notification : filesReceived) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(notification);
                out.flush();
                DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName(UNICAST_RECEIVE_NOTIFICATION_IP), UNICAST_SEND_NOTIFICATION_PORT);
                multicastSocket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
