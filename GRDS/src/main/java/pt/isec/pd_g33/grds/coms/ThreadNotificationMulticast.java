package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.*;

import java.io.*;
import java.net.*;

public class ThreadNotificationMulticast implements Runnable {

    private static final int UNICAST_SEND_NOTIFICATION_PORT = 1000;
    private static final int UNICAST_RECEIVE_NOTIFICATION_PORT = 2000;
    private static final String UNICAST_RECEIVE_NOTIFICATION_IP = "255.255.255.255";

    @Override
    public void run() {
            try{
                DatagramSocket ds = new DatagramSocket(UNICAST_RECEIVE_NOTIFICATION_PORT);
                MulticastSocket multicastSocket = new MulticastSocket(UNICAST_SEND_NOTIFICATION_PORT);
                multicastSocket.setBroadcast(true);

                while(true) {
                    DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                    ds.receive(dp);

                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Notification notification = (Notification) ois.readObject();
                    System.out.println("GRDS received notification: " + notification.getDataType()
                            + "from: " + notification.getFromUsername() + "to: " + notification.getToUsername());

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
}
