package client;

import data.ConnectionMessage;
import data.ConnectionType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        System.out.println("Client");

        /*if (args.length < 2) {
            System.err.println("Missing args: <ip_grds> <port_grds>");
            return;
        }*/

        InetAddress grdsIp;
        int grdsPort;

        try {
            /*grdsIp = InetAddress.getByName(args[0]);
            grdsPort = Integer.parseInt(args[1]);*/

            grdsPort = 9001;
            grdsIp = InetAddress.getByName("127.0.0.1");

            System.out.println("GRDS: " + grdsIp.getHostName() + ":" + grdsPort);

            ConnectionMessage connectionMessage = new ConnectionMessage(ConnectionType.Client);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(connectionMessage);
            oos.flush();

            DatagramSocket ds = new DatagramSocket();
            // ds.setSoTimeout(3000);
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(),baos.toByteArray().length, grdsIp, grdsPort);
            ds.send(dp);

            dp = new DatagramPacket(new byte[4096],4096);
            System.out.println("DatagramPacket enviado ao GRDS");
            ds.receive(dp);

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
            ObjectInputStream ois = new ObjectInputStream(bais);
            connectionMessage = (ConnectionMessage) ois.readObject();

            System.out.println("Server IP: " + connectionMessage.getIp() + ":" + connectionMessage.getPort());
            ds.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.out.println("Exception.: Socket timeout");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

