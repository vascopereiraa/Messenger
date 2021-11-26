package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Data;

import java.io.*;

public class ClientOutputUI{

    private final ServerConnectionManager serverConnectionManager;
    private final ObjectInputStream ois;

    public ClientOutputUI(ServerConnectionManager scm) {
        this.serverConnectionManager = scm;
        ois = serverConnectionManager.getSocketInputStream();
    }

    public void begin() {
        while(true) {

            try {
                Object o = ois.readObject();

                if(o instanceof Data data) {
                    // Login/Register success
                    if(data.getContent().contains("validado")){
                        serverConnectionManager.setUserConnected(true);
                        serverConnectionManager.setUserData(data.getUserData());
                    }
                    System.out.println("Recebi content: " + data.getContent());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return ;
            }
        }
    }
}
