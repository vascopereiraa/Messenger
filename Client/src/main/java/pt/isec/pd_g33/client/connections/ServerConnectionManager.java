package pt.isec.pd_g33.client.connections;

import pt.isec.pd_g33.client.ui.ClientInputUI;
import pt.isec.pd_g33.client.ui.ClientOutputUI;
import pt.isec.pd_g33.shared.UserData;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionManager {

    private final GRDSConnection grdsConnection;

    // Socket
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Connection status
    private boolean serverConnected;
    private UserData userData;

    public ServerConnectionManager(GRDSConnection grdsConnection) {
        this.grdsConnection = grdsConnection;
        this.serverConnected = false;
    }

    public void connectToServer() {
        try {
            socket = new Socket(grdsConnection.getServerIp(), grdsConnection.getServerPort());
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            grdsConnection.connectGRDS();
            connectToServer();
        }

        serverConnected = true;


        // Thread to print everything socket receives
        ClientInputUI clientInputUI = new ClientInputUI(this);
        Thread t = new Thread(clientInputUI);
        t.start();

        // Continuity of this thread to write everything to socket
        ClientOutputUI outputUI = new ClientOutputUI(this);
        outputUI.begin();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Access class data
    public boolean isServerConnected() {
        return serverConnected;
    }

    public boolean isClientConnected() {
        return userData != null;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public ObjectOutputStream getSocketOutputStream() {
        return out;
    }

    public ObjectInputStream getSocketInputStream() {
        return in;
    }
}
