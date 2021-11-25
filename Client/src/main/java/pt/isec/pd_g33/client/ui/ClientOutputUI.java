package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;

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

                if(o instanceof String s) {
                    // Login/Register success
                    if(s.contains("validado"))
                        serverConnectionManager.setUserConnected(true);

                    System.out.println(s);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return ;
            }
        }
    }
}
