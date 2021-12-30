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

// Thread dedicada a receber novos clientes para se conectar por TCP
public class AcceptClientConnectionTCP implements Runnable {

    private final ServerSocket ss;
    private final DatabaseManager databaseManager;
    private final int portToReceiveFiles;
    private final String ipToReceiveFiles;

    // Client information
    private final List<UserInfo> listUsers;
    private String folderPath;

    public AcceptClientConnectionTCP(DatabaseManager databaseManager, List<UserInfo> listUsers, int portToReceiveFiles, String ipToReceiveFiles, ServerSocket ss) {
        this.ss = ss;
        this.databaseManager = databaseManager;
        this.listUsers = listUsers;
        this.portToReceiveFiles = portToReceiveFiles;
        this.ipToReceiveFiles = ipToReceiveFiles;
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

                // Ligações Socket para cada cliente criadas.
                ObjectOutputStream oos = null;
                ObjectInputStream ois = null;
                try {
                    oos = new ObjectOutputStream(sCli.getOutputStream());
                    ois = new ObjectInputStream(sCli.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Add new user to users list
                UserInfo user = new UserInfo(oos);
                listUsers.add(user);

                // Thread lançada para ligacao TCP com cada cliente em especifico que se conecta ao servidor por TCP.
                ClientConnectionTCP cliConn = new ClientConnectionTCP(databaseManager, user, listUsers, oos, ois,
                        portToReceiveFiles, ipToReceiveFiles, folderPath);
                Thread cli = new Thread(cliConn);
                cli.start();
            }
        } catch (IOException e) {
            System.out.println("AcceptClientConnectionTCP thread terminada.");
        }
    }

    public void setServerFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
