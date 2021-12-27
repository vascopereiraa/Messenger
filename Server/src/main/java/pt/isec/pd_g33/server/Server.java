package pt.isec.pd_g33.server;

import pt.isec.pd_g33.server.connections.AcceptClientConnectionTCP;
import pt.isec.pd_g33.server.connections.GRDSConnection;
import pt.isec.pd_g33.server.connections.ThreadMessageReflection;
import pt.isec.pd_g33.server.file.ThreadSendFiles;
import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final String MAIN_SERVER_FOLDER = "./ServerFiles/";

    private static final String MULTICAST_GRDS_IP = "230.30.30.30" ;
    private static final int MULTICAST_GRDS_PORT = 3030;

    private static InetAddress grdsIp;
    private static int grdsPort;
    private static String dbmsLocation;
    private static final List<UserInfo> listUsers = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Server");

        // Verifica argumentos
        if(!argsProcessing(args)) return;

        // Start DB connection
        DatabaseManager databaseManager = new DatabaseManager(dbmsLocation);

        // Thread receive Files
        ThreadSendFiles tsf = new ThreadSendFiles(databaseManager);
        Thread ttrf = new Thread(tsf);
        ttrf.start();

        // Criar o SocketServer para ligações TCP com os Clients
        AcceptClientConnectionTCP acceptClient = new AcceptClientConnectionTCP(databaseManager, listUsers, tsf.getPortToReceiveFiles(), tsf.getIpToReceiveFiles());

        // Regista o IP TCP no GRDS
        GRDSConnection grdsConnection = new GRDSConnection(grdsIp, grdsPort, acceptClient.getMessage());
        if(!grdsConnection.connectGRDS()){
            System.out.println("Server: An error occurred when connecting to GRDS");
            System.exit(1);
        }

        String serverName = grdsConnection.getServerName();
        String folderPath = MAIN_SERVER_FOLDER +  serverName + "/";
        File serverFolder = new File(folderPath);
        if(serverFolder.mkdirs()) System.out.println("Folder Created: " + folderPath);
        acceptClient.setServerFolderPath(folderPath);
        tsf.setFolderPath(folderPath);

        // Cria thread de escuta de notificações do GRDS
        ThreadMessageReflection tmr = new ThreadMessageReflection(listUsers, folderPath, tsf.getPortToReceiveFiles());
        Thread ttmr = new Thread(tmr);
        ttmr.start();

        // Inicia a Thread de aceitação de novos clientes
        Thread clientAcceptionThread = new Thread(acceptClient);
        clientAcceptionThread.start();

        // Heartbeat server, 20 em 20 seg
        ThreadHeartbearServer ths = new ThreadHeartbearServer(grdsConnection);
        Thread tths = new Thread(ths);
        tths.start();

        // Close DB connection
        DatabaseManager.close();
    }

    public static boolean argsProcessing(String[] args) {
        switch(args.length) {
            case 2 -> {
                dbmsLocation = args[0] + ":" + args[1];
                grdsPort = MULTICAST_GRDS_PORT;
                try {
                    grdsIp = InetAddress.getByName(MULTICAST_GRDS_IP);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    System.err.println("""
                                        Couldn't reach GRDS IP specified!
                                        Launch program using: java Server <dbms_ip> <dbms_port> <grds_ip> <grds_port>
                                        """);
                    return false;
                }
                System.out.format("Missing GRDS location -> Using %s:%s to connect to GRDS\n",
                        MULTICAST_GRDS_IP, MULTICAST_GRDS_PORT);
                return true;
            }
            case 4 -> {
                dbmsLocation = args[0] + ":" + args[1];
                grdsPort = Integer.parseInt(args[3]);
                try {
                    grdsIp = InetAddress.getByName(args[2]);
                } catch (UnknownHostException e) {
                    System.err.println("""
                                        Couldn't reach GRDS IP specified!
                                        Launch program using: java Server <dbms_ip> <dbms_port> <grds_ip> <grds_port>
                                        """);
                    return false;
                }
                System.out.format("""
                                    GRDS location: %s:%s
                                    DBMS location: %s
                                    """, grdsIp.getHostName(), grdsPort, dbmsLocation);
                return true;
            }
            default -> {
                System.err.println("""
                                    Missing or Incorrect args
                                    Launch program using: java Server <dbms_ip> <dbms_port> <grds_ip> <grds_port>
                                    """);
                return false;
            }
        }
    }
}
