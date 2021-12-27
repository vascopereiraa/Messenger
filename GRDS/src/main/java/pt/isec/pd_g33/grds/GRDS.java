package pt.isec.pd_g33.grds;

import pt.isec.pd_g33.grds.coms.ThreadHearthbeatManager;
import pt.isec.pd_g33.grds.coms.ThreadNewConnection;
import pt.isec.pd_g33.grds.coms.ThreadNotificationMulticast;
import pt.isec.pd_g33.grds.data.ServerList;

import java.io.IOException;
import java.net.*;

public class GRDS {

    private static final String MULTICAST_IP = "230.30.30.30" ;
    private static final int MULTICAST_PORT = 3030;

    private ServerList serverList;

    public static void main(String[] args) {
        System.out.println("GRDS");

        if (args.length < 1) {
            System.err.println("Missing Args: <Listening_Port>");
            return;
        }

        GRDS grds = new GRDS();
        grds.startThreads(Integer.parseInt(args[0]));
    }

    public void startThreads(int listeningPort) {

        serverList = new ServerList();

        // Start threads to accept new Clients and Servers
        try {
            // Unicast thread
            DatagramSocket datagramSocket = new DatagramSocket(listeningPort);
            ThreadNewConnection unicastThreadAccept = new ThreadNewConnection(datagramSocket, serverList);
            Thread t1 = new Thread(unicastThreadAccept);
            t1.start();

            // Multicast thread
            DatagramSocket ds = new DatagramSocket(MULTICAST_PORT, InetAddress.getByName(MULTICAST_IP));
            System.out.println(ds.getLocalAddress().getHostAddress() + ":" + ds.getLocalPort());
            /*MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress ia = InetAddress.getByName(MULTICAST_IP);
            InetSocketAddress addr = new InetSocketAddress(ia, MULTICAST_PORT);
            NetworkInterface ni = NetworkInterface.getByName("en0");
            multicastSocket.joinGroup(addr, ni);*/

            ThreadNewConnection multicastThreadAccept = new ThreadNewConnection(ds, serverList);
            Thread t2 = new Thread(multicastThreadAccept);
            t2.start();

            //todo: check this Notification thread
            ThreadNotificationMulticast notificationMulticast = new ThreadNotificationMulticast();
            Thread tnm = new Thread(notificationMulticast);
            tnm.start();

            //todo: Thread que vai ler a informação a ser replicada

            // Heartbeat
            ThreadHearthbeatManager heartbeatManager = new ThreadHearthbeatManager(serverList.getServerInfo());
            Thread hearthbeatManager = new Thread(heartbeatManager);
            hearthbeatManager.start();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
