package pt.isec.pd_g33.client;

import pt.isec.pd_g33.client.connections.GRDSConnection;
import pt.isec.pd_g33.client.connections.ServerConnectionManager;

import java.net.*;
import java.util.ArrayList;

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
        String[] loginOrRegister = {"empty"};
        if(!argsProcessing(args)) return;
        do{
            GRDSConnection grdsConnection = new GRDSConnection(grdsIp, grdsPort);
            if (!grdsConnection.connectGRDS()) {
                System.out.println("Client: An error occurred when connecting to GRDS");
                return;
            }

            ServerConnectionManager serverConnectionManager = new ServerConnectionManager(grdsConnection,loginOrRegister);
            // Caso o return (posicao 0 do arraylist retornado) seja 0, quer dizer que o cliente quis sair.
            ArrayList<Object> returnValue = serverConnectionManager.connectToServer();
            if((int) returnValue.get(0) == 0)
                break;
            // Caso contrario, vai existir uma troca de servidor.
            loginOrRegister = (String[]) returnValue.get(1);

        }while(true);

        System.exit(0);
    }
}

