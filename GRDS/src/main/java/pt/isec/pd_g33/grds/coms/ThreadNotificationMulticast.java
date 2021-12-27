package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Notification;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class ThreadNotificationMulticast implements Runnable {

    private static final int UNICAST_SEND_NOTIFICATION_PORT = 1000;
    private static final int UNICAST_RECEIVE_NOTIFICATION_PORT = 2000;
    private static final String UNICAST_RECEIVE_NOTIFICATION_IP = "255.255.255.255";
    private static ArrayList<Notification> filesReceived;

    private static DatagramSocket ds;
    private static MulticastSocket multicastSocket;

    public ThreadNotificationMulticast(ArrayList<Notification> filesReceived) {
        this.filesReceived = filesReceived;
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
                    System.out.println("GRDS received notification: " + notification.getDataType()
                            + "from: " + notification.getFromUsername() + "to: " + notification.getToUsername());

                    // Adicionar as notificações de ficheiro recebidas
                    if(notification.getDataType() == DataType.File)
                        filesReceived.add(notification);

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


    static void synchronizeFiles() {
        for (Notification notification : filesReceived) {
            System.out.println("Item: " + notification);
            //if(!(new File("", notification.getContent()).exists()))
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
