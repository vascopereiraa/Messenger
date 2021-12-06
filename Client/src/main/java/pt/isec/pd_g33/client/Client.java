package pt.isec.pd_g33.client;

import pt.isec.pd_g33.client.connections.GRDSConnection;
import pt.isec.pd_g33.client.connections.ServerConnectionManager;

import java.net.*;

public class Client {

    private static InetAddress grdsIp;
    private static int grdsPort;

    public static boolean argsProcessing(String[] args) {
        if (args.length < 2) {
            System.err.println("""
                    Missing or Incorrect args
                    Launch program using: java Server <grds_ip> <grds_port>
                    """);
            return false;
        }

        try {
            grdsIp = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        grdsPort = Integer.parseInt(args[1]);
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Client");

        if(!argsProcessing(args)) return;

        GRDSConnection grdsConnection = new GRDSConnection(grdsIp, grdsPort);
        if (!grdsConnection.connectGRDS()) {
            System.out.println("Client: An error occurred when connecting to GRDS");
            return;
        }

        ServerConnectionManager serverConnectionManager = new ServerConnectionManager(grdsConnection);
        serverConnectionManager.connectToServer();

        /*
            ThreadServerConnection threadServerConnection = new ThreadServerConnection(grdsConnection);
            Thread serverConnection = new Thread(threadServerConnection);
            serverConnection.start();
        */
    }
}

