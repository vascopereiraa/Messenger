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

                if(o instanceof String)
                    System.out.println((String) o);


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return ;
            }
        }
    }
}
