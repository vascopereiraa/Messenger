package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.Notification;

import java.io.*;

public class ClientOutputUI{

    private final ServerConnectionManager serverConnectionManager;
    private ObjectInputStream ois;

    public ClientOutputUI(ServerConnectionManager scm) {
        this.serverConnectionManager = scm;
        ois = serverConnectionManager.getSocketInputStream();
    }

    public void begin() {
        while(!serverConnectionManager.getExited()) {

            try {
                Object o = ois.readObject();
                if(o instanceof Data data) {
                    // Login/Register success
                    if(data.getContent().contains("sucesso")){
                        serverConnectionManager.setUserConnected(true);
                        serverConnectionManager.setUserData(data.getUserData());
                    }
                    System.out.println("Recebi content: " + data.getContent());

                }
                if(o instanceof Notification notification){
                    System.out.println("Recebeu uma nova notificacao de "+ notification.getDataType().toString()
                            +  " do cliente " + notification.getFromUsername());
                }
                if(o instanceof String s){
                    System.out.println("\n" + s);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return ;
            }
        }
    }
}
