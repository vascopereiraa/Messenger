package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        this.listUsers = listUsers;
    }

    public ConnectionMessage getMessage() {
        System.out.println(ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
        return new ConnectionMessage(ss.getLocalPort(), ConnectionType.Server);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket sCli = ss.accept();

                ObjectOutputStream oos = null;
                ObjectInputStream ois = null;
                try {
                    oos = new ObjectOutputStream(sCli.getOutputStream());
                    ois = new ObjectInputStream(sCli.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Add new user to users list
                UserInfo user = new UserInfo(sCli, oos);
                listUsers.add(user);

                ClientConnectionTCP cliConn = new ClientConnectionTCP(sCli,databaseManager, user, listUsers, oos, ois);
                Thread cli = new Thread(cliConn);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
