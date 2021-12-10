package pt.isec.pd_g33.client.connections;

import pt.isec.pd_g33.client.ui.ClientInputUI;
import pt.isec.pd_g33.client.ui.ClientOutputUI;
import pt.isec.pd_g33.shared.UserData;

import java.io.File;
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
    private boolean userConnected;
    private UserData userData;

    // Sync object
    private Boolean exited = false;

    // Save file location
    private File saveLocation;

    public ServerConnectionManager(GRDSConnection grdsConnection) {
        this.grdsConnection = grdsConnection;
        this.serverConnected = false;
        this.userConnected = false;
    }

    public void connectToServer() {
        try {
            socket = new Socket(grdsConnection.getServerIp(), grdsConnection.getServerPort());
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            serverConnected = false;
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

    public boolean isUserConnected() {
        return userConnected;
    }

    public void setUserConnected(boolean userConnected) {
        this.userConnected = userConnected;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public UserData getUserData() {
        return this.userData;
    }

    public ObjectOutputStream getSocketOutputStream() {
        return out;
    }

    public ObjectInputStream getSocketInputStream() {
        return in;
    }

    // File save location
    public boolean setSaveLocation(String saveLocation) {
        File saveLoc = new File(saveLocation);
        if (saveLoc.canWrite()) {
            this.saveLocation = saveLoc;
            return true;
        }
        return false;
    }

    public File getSaveLocation() {
        return saveLocation;
    }

    // Disconnect Client
    public void disconnectClient() {
        exited = true;
    }

    public boolean getExited() {
        return exited;
    }
}
