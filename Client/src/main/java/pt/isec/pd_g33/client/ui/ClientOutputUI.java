package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ClientOutputUI {

    private final ServerConnectionManager serverConnectionManager;
    private ObjectInputStream ois;

    public ClientOutputUI(ServerConnectionManager scm) {
        this.serverConnectionManager = scm;
        ois = serverConnectionManager.getSocketInputStream();
    }

    public void begin() {
        while (!serverConnectionManager.getExited()) {

            try {
                Object o = ois.readObject();
                if (o instanceof Data data) {
                    // Login/Register success
                    if (data.getContent().contains("sucesso")) {
                        serverConnectionManager.setUserConnected(true);
                        serverConnectionManager.setUserData(data.getUserData());
                    }
                    System.out.println("Recebi content: " + data.getContent());

                }
                if (o instanceof Notification notification) {
                    System.out.print("Recebeu uma notificação: ");
                    switch (notification.getDataType()) {
                        case Message -> {
                            if (notification.getToGroupId() != 0)
                                System.out.println("Tem uma mensagem por visualizar no grupo " + notification.getToGroupId() + ".:" +
                                        notification.getToGroupName() + " enviada por " + notification.getFromUsername() + ".");
                            else
                                System.out.println("O seu contacto " + notification.getFromUsername() + " enviou-lhe uma mensagem.");
                        }
                        case File -> {
                            if (notification.getToGroupId() != 0)
                                System.out.println("Tem um ficheiro por visualizar no grupo " + notification.getToGroupId() + ".:" +
                                        notification.getToGroupName() + " enviado por " + notification.getFromUsername() + ".");
                            else
                                System.out.println("O seu contacto " + notification.getFromUsername() + " enviou-lhe um ficheiro.");
                        }
                        case Contact -> System.out.println("Recebi um pedido de contacto por parte do utilizador " + notification.getFromUsername() + ".");
                        case Group ->{
                            if(notification.getContent().contains("aceite"))
                                System.out.println("Seu pedido de adesão ao grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() + " foi aceite.");
                            else
                                System.out.println("Foi removido do grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() + " pelo administrador.");
                        }
                    }
                }
                if (o instanceof String s) {
                    System.out.println("\n" + s);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
