package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.connections.ClientConnectionTCP;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AcceptClientConnectionTCP implements Runnable {

    private ServerSocket ss;
    private DatabaseManager databaseManager;

    // Client information
    private List<UserInfo> listUsers;

    public AcceptClientConnectionTCP(DatabaseManager databaseManager, List<UserInfo> listUsers) {
        try {
            ss = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                // Add new user to users list
                UserInfo user = new UserInfo(sCli);
                listUsers.add(user);

                ClientConnectionTCP cliConn = new ClientConnectionTCP(sCli,databaseManager, user);
                Thread cli = new Thread(cliConn);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
