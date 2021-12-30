package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.client.files.RequestFileProc;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.MenuOption;
import pt.isec.pd_g33.shared.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;

public class ClientOutputUI {

    private final ServerConnectionManager serverConnectionManager;
    private ObjectInputStream ois;

    public ClientOutputUI(ServerConnectionManager scm,ObjectInputStream ois) {
        this.serverConnectionManager = scm;
        this.ois = ois;
        this.ois = serverConnectionManager.getSocketInputStream();
    }

    public int begin() {
        while (!serverConnectionManager.getExited()) {
            try {
                Object o = ois.readObject();
                if (o instanceof Data data) {
                    if(data.getMenuOptionSelected() == MenuOption.REQUEST_FILE_FROM_CONTACT ||
                        data.getMenuOptionSelected() == MenuOption.REQUEST_FILE_FROM_GROUP) {
                        RequestFileProc rfc = new RequestFileProc(serverConnectionManager.getSaveLocation(),
                                data.getContent(), data.getReadState(), data.getToUserId());
                        Thread trfc = new Thread(rfc);
                        trfc.start();

                        trfc.join();
                        System.out.println("File " + data.getContent() + " saved on " + serverConnectionManager.getSaveLocation().toString());
                    }

                    // Login/Register success
                    if (data.getContent().contains("sucesso")) {
                        serverConnectionManager.setUserConnected(true);
                        serverConnectionManager.setUserData(data.getUserData());
                    }
                    System.out.println("Info servidor: " + data.getContent());
                }
                if (o instanceof Notification notification) {
                    System.out.print("Recebeu uma notificação: ");
                    switch (notification.getDataType()) {
                        case Message -> {
                            // Servidor terminou ordenadamente, terminando assim o cliente, tal como o exit.
                            if(notification.getContent().equals("serverTerminated")){
                                System.out.println("Servidor fechou. Cliente vai terminar. Prima [ENTER] para confirmar.");
                                return 0;
                            }
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
            }catch (IOException | ClassNotFoundException | InterruptedException e) {
                if(serverConnectionManager.getExited())
                    return 0;
                System.out.println("O servidor terminou a conexão. Pressione [ENTER] para obter um novo servidor.");
                return 1;
            }
        }
        return 0;
    }
}
