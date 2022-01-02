package pt.isec.pd_g33.server;

import pt.isec.pd_g33.server.Heartbeat.ThreadHeartbearServer;
import pt.isec.pd_g33.server.Heartbeat.ThreadHeartbeatClient;
import pt.isec.pd_g33.server.connections.AcceptClientConnectionTCP;
import pt.isec.pd_g33.server.connections.GRDSConnection;
import pt.isec.pd_g33.server.connections.ThreadMessageReflection;
import pt.isec.pd_g33.server.file.ThreadSendFiles;
import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.ConnectionMessage;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final String MAIN_SERVER_FOLDER = "./ServerFiles/";

    private static final String MULTICAST_GRDS_IP = "230.30.30.30" ;
    private static final int MULTICAST_GRDS_PORT = 3030;
    public static final int REFLECTION_PORT = 1000;

    private static InetAddress grdsIp;
    private static int grdsPort;
    private static String dbmsLocation;
    private static DatabaseManager databaseManager;
    private static String folderPath;
    private static final List<UserInfo> listUsers = new ArrayList<>();

    // Criação dos Sockets para poder fecha-los quando for necessário para conseguir fechar threads.
    private static ServerSocket sendFilesSS, acceptClientConnectionSS;
    private static MulticastSocket MessageReflectionMulticastSocket;
    private static ConnectionMessage connectionMessage;

    public static void main(String[] args) {
        System.out.println("Server");

        // Criação dos ServerSockets e MulticastSocket.
        try {
            sendFilesSS = new ServerSocket(0);
            acceptClientConnectionSS = new ServerSocket(0);
            MessageReflectionMulticastSocket = new MulticastSocket(REFLECTION_PORT);
        } catch (IOException e) {
            System.err.println("Erro na criação dos Sockets.");
        }

        // Verifica argumentos
        if(!argsProcessing(args)) return;

        // Start DB connection
        databaseManager = new DatabaseManager(dbmsLocation);

        // Thread send Files
        ThreadSendFiles tsf = new ThreadSendFiles(sendFilesSS);
        Thread ttsf = new Thread(tsf);
        ttsf.start();

        // Criar o SocketServer para ligações TCP com os Clients
        AcceptClientConnectionTCP acceptClient = new AcceptClientConnectionTCP(databaseManager, listUsers, tsf.getPortToReceiveFiles(), tsf.getIpToReceiveFiles(),acceptClientConnectionSS);

        connectionMessage = acceptClient.getMessage();
        // Regista o IP TCP no GRDS
        GRDSConnection grdsConnection = new GRDSConnection(grdsIp, grdsPort,connectionMessage);
        if(!grdsConnection.connectGRDS()){
            System.out.println("Server: An error occurred when connecting to GRDS");
            System.exit(1);
        }

        String serverName = grdsConnection.getServerName();
        folderPath = MAIN_SERVER_FOLDER +  serverName + "/";
        File serverFolder = new File(folderPath);
        if(serverFolder.mkdirs()) System.out.println("Folder Created: " + folderPath);
        acceptClient.setServerFolderPath(folderPath);
        tsf.setFolderPath(folderPath);


        // Cria thread de escuta de notificações do GRDS
        ThreadMessageReflection tmr = new ThreadMessageReflection(listUsers, folderPath, tsf.getPortToReceiveFiles(), MessageReflectionMulticastSocket);
        Thread ttmr = new Thread(tmr);
        ttmr.start();

        // Inicia a Thread de aceitação de novos clientes
        Thread clientAcceptionThread = new Thread(acceptClient);
        clientAcceptionThread.start();

        // Heartbeat server, 20 em 20 seg
        ThreadHeartbearServer ths = new ThreadHeartbearServer(grdsConnection);
        Thread tths = new Thread(ths);
        tths.start();

        // Heartbeat client, 30 em 30 seg
        ThreadHeartbeatClient thc = new ThreadHeartbeatClient(databaseManager);
        Thread tthc = new Thread(thc);
        tthc.start();

        // Thread para terminar server intencionalmente
        ThreadTerminateServer tts = new ThreadTerminateServer();
        Thread ttts = new Thread(tts);
        ttts.start();

        // Espera o fim do servidor, caso termine ordenadamente.
        try {
            // Espera que o user faça EXIT do server.
            ttts.join();
            // Informar os clientes para terminar
            listUsers.forEach(u -> databaseManager.changeUserStatus((int) databaseManager.getUserID(u.getUsername()), 0));
            ThreadMessageReflection.terminaClientes(connectionMessage);
            // Indica para a Thread Heartbeat Server e Client terminar
            tths.stop();
            tths.join();
            tthc.stop();
            tthc.join();
            System.out.println("Thread TerminateServer files ended");
            // Termina Thread send files
            sendFilesSS.close();
            ttsf.join();
            System.out.println("Thread send files ended");
            // Terminar Thread aceita clientes
            acceptClientConnectionSS.close();
            clientAcceptionThread.join();
            // Termina Thread MessageReflection
            MessageReflectionMulticastSocket.close();
            ttmr.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro no termino da thread.");
        }
        System.out.println("Servidor foi terminado");
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
