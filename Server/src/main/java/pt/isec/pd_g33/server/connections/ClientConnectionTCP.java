package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Notification;
import pt.isec.pd_g33.shared.Register;
import pt.isec.pd_g33.shared.UserData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ClientConnectionTCP implements Runnable {

    private static final int UNICAST_NOTIFICATION_PORT = 2000;
    private final Socket sCli;
    private final DatabaseManager databaseManager;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private Object dataReceived;
    private final UserInfo userInfo;
    private List<UserInfo> listUsers;

    public ClientConnectionTCP(Socket scli, DatabaseManager databaseManager, UserInfo userInfo, List<UserInfo> listUsers,
                               ObjectOutputStream oos, ObjectInputStream ois){
        this.sCli = scli;
        this.databaseManager = databaseManager;
        this.userInfo = userInfo;
        this.listUsers = listUsers;

        //todo: porque é que a conecção com a BD se fecha ?
        this.databaseManager.setConnection();

        this.oos = oos;
        this.ois = ois;
    }

    @Override
    public void run() {
        while(true){

            try {
                dataReceived = ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("IOException: Cliente fechou a conexão");
                e.printStackTrace();
                break;
            }

            if(dataReceived instanceof Login) {
                if(!loginDatabase()) return;
            }
            if(dataReceived instanceof Register){
                if(!registerDatabase()) return;
            }
            if(dataReceived instanceof Data){
                processData((Data)dataReceived);
            }
            if(dataReceived instanceof Notification n){
                System.out.println("ClientConnectionTCP: Recebi uma notificacao! ");
                processNotification(n);
            }
        }
    }

    private void processData(Data dataReceived) {

        switch (dataReceived.getMenuOptionSelected()) {
            case -1 -> changeUserStatus(dataReceived.getToUserId());

            case 1 -> {
                if(databaseManager.updateUser(
                        dataReceived.getUserData().getName(),
                        dataReceived.getUserData().getUsername(),
                        dataReceived.getUserData().getPassword(),
                        dataReceived.getToUserId())){
                    writeToSocket("Utilizador atualizado com sucesso!");
                } else
                    writeToSocket("Não foi possível atualizar o utilizador");
            }
            case 2 -> {
                writeToSocket(databaseManager.listUsers());
            }
            case 3 -> {
                writeToSocket(databaseManager.searchUserByName(dataReceived.getContent()));
            }
            case 4 -> {
                writeToSocket(databaseManager.listContacts((int)databaseManager.getUserID(userInfo.getUsername())));
            }
            case 6 -> {
                writeToSocket(databaseManager.deleteContact(dataReceived.getUserData().getUsername(), dataReceived.getToUserUsername()));
            }
            // lista contactos pendentes
            case 7 -> writeToSocket(databaseManager.pendConact(dataReceived.getContent()));
            // Aceitar contacto pendente
            case 8 -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(),dataReceived.getContent(),"accept"));
            // Eliminar contacto pendente
            case 9 -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(),dataReceived.getContent(),"reject"));
            //todo: Enviar mensagens. A funcionar com clientes
            case 10 -> {
                //todo: verificar se o cliente tem o contacto ou pertence ao grupo
                sendMessage(dataReceived);
            }
            // Criar grupo
            case 11 -> {
                writeToSocket(databaseManager.addNewGroup(dataReceived.getContent(), dataReceived.getUserData().getUserID()));
                databaseManager.joinGroup(dataReceived.getUserData().getUserID(), dataReceived.getContent(), "approved");
            }
            case 12 -> {
                if(databaseManager.joinGroup(dataReceived.getUserData().getUserID(), dataReceived.getContent(), "pending")) {
                    writeToSocket("Foi enviado um pedido de adesão ao administrador do grupo");
                    processNotification(new Notification(dataReceived.getUserData().getUsername(), databaseManager.getGroupAdmin(dataReceived.getContent()), DataType.JoinGroup));
                }else{
                    writeToSocket("Ocorreu um erro. Não foi possível aderir a grupo");
                }
            }
            default -> {
                System.err.println("Opção invalidade de menu");
            }
        }
    }

    private void sendMessage(Data dataReceived) {
        // Quer dizer que é uma mensagem para o grupo
        if(dataReceived.getToUserId() != 0){
            //todo: criar esta query
            if(databaseManager.isGroupMember(dataReceived.getUserData().getUsername(),dataReceived.getToGroupId())){
                databaseManager.addMessageToGroup(dataReceived);
                processNotification(new Notification(dataReceived.getUserData().getUsername(),
                        databaseManager.getGroupNameById(dataReceived.getToGroupId()), // Obter nome do grupo pelo ID
                        dataReceived.getDataType()));
            }else{
                writeToSocket("O utilizador não pertence a este grupo! ");
            }
        }else{ // Mensagem para uma pessoa
            //todo: criar esta query
            if(databaseManager.isContact(dataReceived.getUserData().getUsername(),dataReceived.getToUserUsername())){
                databaseManager.addMessageToUser(dataReceived);
                processNotification(new Notification(dataReceived.getUserData().getUsername(),
                        dataReceived.getToUserUsername(), // Obter username do utilizador
                        dataReceived.getDataType()));
            }
        }
    }

    private boolean loginDatabase(){
        try {
            UserData userData = databaseManager.checkUserLogin(((Login) dataReceived).getUsername(), ((Login) dataReceived).getPassword());
            if (userData != null){
                changeUserStatus(userData.getUserID());
                userInfo.setUsername(((Login) dataReceived).getUsername());
                oos.writeObject(new Data("Login validado com sucesso!", userData));
            }
            else {
                oos.writeObject(new Data("Login invalido."));
            }
            oos.flush();

        } catch (IOException e) {
            System.err.println("Login IOExecption");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean registerDatabase(){
        try {
            UserData userData = databaseManager.insertUser( ((Register) dataReceived).getName(),
                    ((Register) dataReceived).getUsername(),
                    ((Register) dataReceived).getPassword());
            if (userData != null) {
                userInfo.setUsername(((Register) dataReceived).getUsername());
                oos.writeObject(new Data("Registo efetuado com sucesso",userData));
            } else {
                oos.writeObject(new Data("Registo invalido."));
            }
            oos.flush();
        } catch (IOException e) {
            System.err.println("Register IOExecption");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void processNotification(Notification notification) {

        if(notification.getDataType() == DataType.Contact){
            System.out.println("Notificação é do tipo Contact");
            databaseManager.insertContact(notification.getFromUsername(),notification.getToUsername());
        }

        //todo: Verificar se cliente pertence a este servidor, caso pertença, não precisa avisar
        listUsers.forEach(u -> {
            if(u.getUsername().equals(notification.getToUsername())){
                System.out.println("\n\nExiste o cliente no mesmo servidor");
                u.writeSocket(notification);
                return;
            }
        });

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(notification);
            oos.flush();

            DatagramSocket ds = new DatagramSocket();
            // todo: Acertar IPS -> dp tem de ter o IP do GRDS passado pela cmdLine
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName("127.0.0.1"), UNICAST_NOTIFICATION_PORT);
            ds.send(dp);
        } catch (IOException e) {
            System.err.println("IOException: processNotification");
            e.printStackTrace();
        }
    }

    private void writeToSocket(Object o) {
        userInfo.writeSocket(o);
    }

    private void changeUserStatus(int userId) {
        databaseManager.changeUserStatus(userId);
    }
}
