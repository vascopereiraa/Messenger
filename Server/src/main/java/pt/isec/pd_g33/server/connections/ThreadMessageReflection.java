package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.shared.Notification;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.List;

public class ThreadMessageReflection implements Runnable {

    public static final String REFLECTION_IP = "230.30.30.31" ;
    public static final int REFLECTION_PORT = 1000;

    private static List<UserInfo> listUsers;

    public ThreadMessageReflection(List<UserInfo> listUsers){
        this.listUsers = listUsers;
    }

    @Override
    public void run() {
        MulticastSocket multicastSocket = null;
        try {
            multicastSocket = new MulticastSocket(REFLECTION_PORT);

        } catch (IOException e) {
            System.err.println("IOException: Multicast");
            e.printStackTrace();
        }


        while(true){
            try {
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                multicastSocket.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Notification notification = (Notification) ois.readObject();

                //todo: debug
                System.out.println("Recebi uma mensagem refletida: " + notification.getFromUsername() + " : "
                        + notification.getToUsername() + " : " + notification.getDataType());;

                // Envia notificação ao cliente correto caso ele esteja connectado a este servidor
                listUsers.forEach(u -> {
                    if (notification.getToUsername().equals(u.getUsername())) {
                        System.out.println("\nVou enviar a notificação ao utilizador: " + u.getUsername()
                        + " Notificacao: " + notification.getFromUsername() + " : " + notification.getToUsername()
                        + " : " + notification.getDataType());
                        u.writeSocket(notification);
                    }
                });

            } catch (IOException e) {
                System.err.println("IOException: Multicast reading");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
