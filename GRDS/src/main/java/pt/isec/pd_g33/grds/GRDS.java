package pt.isec.pd_g33.grds;

import pt.isec.pd_g33.grds.coms.ThreadHearthbeatManager;
import pt.isec.pd_g33.grds.coms.ThreadNewConnection;
import pt.isec.pd_g33.grds.coms.ThreadNotificationMulticast;
import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.Notification;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class GRDS {

    private static final String MULTICAST_IP = "230.30.30.30" ;
    private static final int MULTICAST_PORT = 3030;

    private ServerList serverList;
    private ArrayList<Notification> filesReceived;

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
        filesReceived = new ArrayList<>();

        // Start threads to accept new Clients and Servers
        try {
            // Unicast thread
            DatagramSocket datagramSocket = new DatagramSocket(listeningPort);
            ThreadNewConnection unicastThreadAccept = new ThreadNewConnection(datagramSocket, serverList,filesReceived);
            Thread t1 = new Thread(unicastThreadAccept);
            t1.start();

            // Multicast thread
            /*DatagramSocket ds = new DatagramSocket(MULTICAST_PORT, InetAddress.getByName(MULTICAST_IP));
            System.out.println(ds.getLocalAddress().getHostAddress() + ":" + ds.getLocalPort());*/
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress ia = InetAddress.getByName(MULTICAST_IP);
            InetSocketAddress addr = new InetSocketAddress(ia, MULTICAST_PORT);
            NetworkInterface ni = NetworkInterface.getByName("en0");
            multicastSocket.joinGroup(addr, ni);

            ThreadNewConnection multicastThreadAccept = new ThreadNewConnection(multicastSocket, serverList,filesReceived);
            Thread t2 = new Thread(multicastThreadAccept);
            t2.start();

            //todo: check this Notification thread
            ThreadNotificationMulticast notificationMulticast = new ThreadNotificationMulticast(filesReceived);
            Thread tnm = new Thread(notificationMulticast);
            tnm.start();

            //todo: Thread que vai ler a informação a ser replicada

            // Heartbeat
            ThreadHearthbeatManager heartbeatManager = new ThreadHearthbeatManager(serverList.getServerInfo());
            Thread hearthbeatManager = new Thread(heartbeatManager);
            hearthbeatManager.start();



        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
