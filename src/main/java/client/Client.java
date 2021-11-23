package client;

import messages.ConnectionMessage;
import messages.ConnectionType;

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

}

