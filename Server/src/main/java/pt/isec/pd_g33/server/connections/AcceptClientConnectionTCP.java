package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.connections.ClientConnectionTCP;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

public class AcceptClientConnectionTCP implements Runnable {

    private ServerSocket ss;
    private DatabaseManager databaseManager;

    public AcceptClientConnectionTCP(DatabaseManager db) {

        this.databaseManager = databaseManager;

        try {
            ss = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConnectionMessage getMessage() {
        return new ConnectionMessage(ss.getInetAddress(), ss.getLocalPort(), ConnectionType.Server);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket sCli = ss.accept();

                ClientConnectionTCP cliConn = new ClientConnectionTCP(sCli,databaseManager);
                Thread cli = new Thread(cliConn);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
