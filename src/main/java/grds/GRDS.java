package grds;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

public class GRDS {

    private static final String MULTICAST_IP = "230.30.30.30" ;
    private static final int MULTICAST_PORT = 3030;

    public static void main(String[] args) {
        System.out.println("GRDS");

        /*if (args.length < 1) {
            System.err.println("Missing Args: <Listening_Port>");
            return;
        }*/

        // int listeningPort = Integer.parseInt(args[0]);
        int listeningPort = 9001;

        // Start thread to accept new Clients and Servers
        try {
            // Unicast thread
            ThreadNewClient unicastThreadAccept = new ThreadNewClient(new DatagramSocket(listeningPort), "uni");
            Thread t1 = new Thread(unicastThreadAccept);
            t1.start();

            // Multicast thread
            MulticastSocket ms = new MulticastSocket(MULTICAST_PORT);
            InetAddress ia = InetAddress.getByName(MULTICAST_IP);
            InetSocketAddress addr = new InetSocketAddress(ia, MULTICAST_PORT);
            NetworkInterface ni = NetworkInterface.getByName("en0");
            ms.joinGroup(addr, ni);
            ThreadNewClient multicastThreadAccept = new ThreadNewClient(ms, "multi");
            Thread t2 = new Thread(multicastThreadAccept);
            t2.start();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
