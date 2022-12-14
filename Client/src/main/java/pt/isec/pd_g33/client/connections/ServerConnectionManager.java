package pt.isec.pd_g33.client.connections;

import pt.isec.pd_g33.client.ThreadHeartbeatClient;
import pt.isec.pd_g33.client.ui.ClientInputUI;
import pt.isec.pd_g33.client.ui.ClientOutputUI;
import pt.isec.pd_g33.shared.UserData;

import java.io.File;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerConnectionManager {

    private final GRDSConnection grdsConnection;

    // Socket
    private Socket socket;
    private ObjectInputStream oisClientInput;
    private ObjectOutputStream out;

    // Connection status
    private boolean serverConnected;
    private boolean userConnected;
    private UserData userData;

    // Sync object
    private Boolean exited = false;

    // Save file location
    private File saveLocation;

    // Atualizar para caso seja preciso trocar de servidor, o login ser feito automatico caso o user esteja ligado ja
    private final String[] loginOrRegister;
    private final ArrayList<Object> returnValues = new ArrayList<>(Arrays.asList(0,new String[]{"empty"}));
    private boolean exit;

    public ServerConnectionManager(GRDSConnection grdsConnection, String[] loginOrRegister) {
        this.loginOrRegister = loginOrRegister;
        this.grdsConnection = grdsConnection;
        this.serverConnected = false;
        this.userConnected = false;
    }

    public ArrayList<Object> connectToServer() {
        try {
            socket = new Socket(grdsConnection.getServerIp(), grdsConnection.getServerPort());
            oisClientInput = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            serverConnected = false;
            System.err.println("Não existem servidores disponiveis\n");
            returnValues.set(0,0);
            return returnValues; // Da return do ArrayList com a posicao '0' (do return) devidamente preenchida
        }
        serverConnected = true;

        // Thread para obter input do user para mandar pedidos ao servidor
        ClientInputUI clientInputUI = new ClientInputUI(this,loginOrRegister, oisClientInput);
        Thread t = new Thread(clientInputUI);
        t.start();

        // Criação da thread para envio de heartbeats para o servidor
        ThreadHeartbeatClient ths = new ThreadHeartbeatClient(this);
        Thread tths = new Thread(ths);
        tths.start();


        // Classe para receber respostas dos servidores, resposta a pedidos e notificações.
        ClientOutputUI outputUI = new ClientOutputUI(this,oisClientInput);
        returnValues.set(0,outputUI.begin()); // Atualiza return do arrayList com o return de outputUI.begin()

        t.stop();
        try {
            t.join();
            // Para manter atualizado dados de login do user
            returnValues.set(1,clientInputUI.getLoginOrRegister());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        serverConnected = false;


        return returnValues;
    }

    // Access class data
    public boolean isServerConnected() {
        return serverConnected;
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
        return oisClientInput;
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
        return exit;
    }
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean getServerConnected(){
        return serverConnected;
    }

    public boolean isUserConnected() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userConnected;
    }

}
