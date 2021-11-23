package server.connections;

import messages.ConnectionMessage;
import messages.ConnectionType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptClientConnectionTCP implements Runnable {

    private ServerSocket ss;

    public AcceptClientConnectionTCP() {
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

                ClientConnectionTCP cliConn = new ClientConnectionTCP(sCli);
                Thread cli = new Thread(cliConn);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
