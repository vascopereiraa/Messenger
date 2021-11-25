package pt.isec.pd_g33.server;

import pt.isec.pd_g33.server.connections.AcceptClientConnectionTCP;
import pt.isec.pd_g33.server.connections.GRDSConnection;
import pt.isec.pd_g33.server.database.DatabaseConnection;
import pt.isec.pd_g33.server.database.DatabaseManager;

import java.net.*;

public class Server {

    private static final String MULTICAST_GRDS_IP = "230.30.30.30" ;
    private static final int MULTICAST_GRDS_PORT = 3030;

    private static InetAddress grdsIp;
    private static int grdsPort;
    private static String dbmsLocation;

    public static void main(String[] args) {
        System.out.println("Server");

        // Verifica argumentos
        if(!argsProcessing(args)) return;

        // Start DB connection
        DatabaseConnection databasesConnection = new DatabaseConnection(dbmsLocation);
        DatabaseManager databaseManager = new DatabaseManager(databasesConnection);


        // Criar o SocketServer para ligações TCP com os Clients
        AcceptClientConnectionTCP acceptClient = new AcceptClientConnectionTCP(databaseManager);

        // Regista o IP TCP no GRDS
        GRDSConnection grdsConnection = new GRDSConnection(grdsIp, grdsPort, acceptClient.getMessage());
        if(!grdsConnection.connectGRDS()){
            System.out.println("Server: An error occurred when connecting to GRDS");
            return;
        }

        // Inicia a Thread de aceitação de novos clientes
        Thread clientAcceptionThread = new Thread(acceptClient);
        clientAcceptionThread.start();

        // Heartbeat server, 20 em 20 seg
        ThreadHeartbearServer ths = new ThreadHeartbearServer(grdsConnection);
        Thread tths = new Thread(ths);
        tths.start();

        // Close DB connection
        databasesConnection.close();
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
