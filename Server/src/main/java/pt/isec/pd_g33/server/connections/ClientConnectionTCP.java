package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.server.file.ThreadReceiveFiles;
import pt.isec.pd_g33.shared.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class ClientConnectionTCP implements Runnable {

    private static final int UNICAST_NOTIFICATION_PORT = 2000;
    private final DatabaseManager databaseManager;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private int portToReceiveFiles;
    private String ipToReceiveFiles;

    private Object dataReceived;
    private final UserInfo userInfo;
    private List<UserInfo> listUsers;

    private String folderPath;

    public ClientConnectionTCP(DatabaseManager databaseManager, UserInfo userInfo, List<UserInfo> listUsers,
                               ObjectOutputStream oos, ObjectInputStream ois, int portToReceiveFiles,
                               String ipToReceiveFiles, String folderPath) {
        this.databaseManager = databaseManager;
        this.userInfo = userInfo;
        this.listUsers = listUsers;

        this.databaseManager.setConnection();

        this.oos = oos;
        this.ois = ois;
        this.portToReceiveFiles = portToReceiveFiles;
        this.ipToReceiveFiles = ipToReceiveFiles;

        this.folderPath = folderPath;
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

            if(dataReceived instanceof Login login) {
                if(!loginDatabase(login)) return;
            }
            if(dataReceived instanceof Register register){
                if(!registerDatabase(register)) return;
            }
            if(dataReceived instanceof Data data){
                processData(data);
            }
            if(dataReceived instanceof Notification notification){
                System.out.println("ClientConnectionTCP: Recebi uma notificacao! ");
                processNotification(notification);
            }
        }
    }

    private void processData(Data dataReceived) {
        switch (dataReceived.getMenuOptionSelected()) {
            // User
            case EDIT_USER -> {
                writeToSocket(databaseManager.updateUser(
                        dataReceived.getUserData().getName(),
                        dataReceived.getUserData().getUsername(),
                        dataReceived.getUserData().getPassword(),
                        dataReceived.getToUserId()));
            }
            case LIST_USERS -> writeToSocket(databaseManager.listUsers());
            case SEARCH_USER -> writeToSocket(databaseManager.searchUserByName(dataReceived.getContent()));

            // Groups
            case CREATE_GROUP -> writeToSocket(databaseManager.addNewGroup(dataReceived.getContent(), dataReceived.getUserData().getUserID()));
            case LIST_GROUPS -> writeToSocket(databaseManager.listGroups());
            case JOIN_GROUP -> {
                if(databaseManager.joinGroup(dataReceived.getUserData().getUserID(), dataReceived.getToUserId(),"pending")) {
                    writeToSocket("Foi enviado um pedido de adesão ao administrador do grupo");
                    processNotification(new Notification(dataReceived.getUserData().getUsername(), databaseManager.getGroupAdmin(dataReceived.getToUserId()), DataType.JoinGroup));
                } else
                    writeToSocket("Ocorreu um erro. Não foi possível aderir a grupo. Certifique-se que o grupo existe e que já não pertence a ele.");
            }
            case MEMBER_ACCEPT -> {
                if(databaseManager.acceptOrRejectGroupMember(dataReceived.getToGroupId(), dataReceived.getContent(),"accept",dataReceived.getUserData().getUsername())) {
                    writeToSocket(dataReceived.getContent().trim() + " foi adicionado ao grupo");
                    processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getContent(), DataType.Message));
                } else
                    writeToSocket("Ocorreu um erro. Não foi possível aceitar um novo membro no grupo! Certifique-se que tem os dados corretos e é administrador!");
            }
            case MEMBER_REMOVE -> {
                if(databaseManager.acceptOrRejectGroupMember(dataReceived.getToGroupId(), dataReceived.getContent(),"reject",dataReceived.getUserData().getUsername())) {
                    writeToSocket(dataReceived.getContent().trim() + " foi removido");
                    processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getContent(), DataType.Message));
                } else
                    writeToSocket("Ocorreu um erro. Não foi possível aceitar um novo membro no grupo! Certifique-se que tem os dados corretos e é administrador!");
            }
            case RENAME_GROUP -> writeToSocket(databaseManager.updateGroupName(dataReceived.getContent(), dataReceived.getToGroupId(),dataReceived.getUserData().getUsername()));
            case DELETE_GROUP -> writeToSocket(databaseManager.deleteGroup(dataReceived.getContent(), dataReceived.getToGroupId()));
            case LEAVE_GROUP -> writeToSocket(databaseManager.leaveGroup(dataReceived.getUserData(), dataReceived.getToUserId(), "approved"));

            // Contacts
            case LIST_CONTACTS -> writeToSocket(databaseManager.listContacts((int) databaseManager.getUserID(userInfo.getUsername())));
            case PENDING_CONTACT -> writeToSocket(databaseManager.pendingContact(dataReceived.getContent()));
            case DELETE_CONTACT -> writeToSocket(databaseManager.deleteContact(dataReceived.getUserData().getUsername(), dataReceived.getContent()));
            case ACCEPT_CONTACT -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(),dataReceived.getContent(),"accept"));
            case REJECT_CONTACT -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(),dataReceived.getContent(),"reject"));
            case ADD_CONTACT -> {
                writeToSocket(databaseManager.insertContact(dataReceived.getContent(),dataReceived.getToUserUsername()));
                processNotification(new Notification(dataReceived.getContent(),dataReceived.getToUserUsername(),DataType.Contact));
            }
            // Messages
            case SEND_MSG_TO_CONTACT -> {
                if (databaseManager.addMsgAndFilesUsers(dataReceived)) {
                    writeToSocket("[SUCCESS] Message sent successfully to " + dataReceived.getToUserUsername());
                    processNotification(new Notification(dataReceived.getUserData().getUsername(),
                            dataReceived.getToUserUsername(), dataReceived.getDataType()));
                }else
                    writeToSocket("[WARNING] " + dataReceived.getToUserUsername() + " does not make part of your contacts list");
            }
            case SEND_MSG_TO_GROUP -> {
                if(databaseManager.addMsgAndFilesGroups(dataReceived)) {
                    writeToSocket("[SUCCESS] Message sent successfully to " + databaseManager.getGroupNameById(dataReceived.getToGroupId()));
                    processNotification(new Notification(dataReceived.getUserData().getUsername(),
                            databaseManager.getGroupNameById(dataReceived.getToGroupId()), // Obter nome do grupo pelo ID
                            dataReceived.getDataType()));
                } else
                    writeToSocket("[WARNING] You are not a member of this group!");
            }
            case LIST_MSG_CONTACT -> writeToSocket(databaseManager.listUserMsg(dataReceived.getContent(),dataReceived.getToUserUsername()));
            case LIST_MSG_GROUP -> writeToSocket(databaseManager.listGroupMsg(dataReceived.getContent(),Integer.parseInt(dataReceived.getToUserUsername())));
            case LIST_UNSEEN -> writeToSocket(databaseManager.listUnseen(dataReceived.getContent()));
            case DELETE_MESSAGE -> writeToSocket(databaseManager.deleteMsg(dataReceived.getContent(),dataReceived.getToGroupId() /* groupid == MSG id */));

            // Files
            case SEND_FILE_TO_CONTACT -> {
                ThreadReceiveFiles threadReceiveFiles = new ThreadReceiveFiles(dataReceived.getReadState(),dataReceived.getToUserId(),folderPath,dataReceived.getContent());
                Thread received = new Thread(threadReceiveFiles);
                if(databaseManager.addMsgAndFilesUsers(dataReceived)) {
                    received.start();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    writeToSocket("[SUCCESS] File sent successfully to " + dataReceived.getToUserUsername());
                    /*processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getToUserUsername(),
                                            DataType.File,dataReceived.getContent(),ipToReceiveFiles,portToReceiveFiles));*/
                }else{
                    writeToSocket("[WARNING] " + dataReceived.getToUserUsername() + " does not make part of your contacts list");
                }
            }

            // Exit
            case EXIT -> {
                changeUserStatus(dataReceived.getToUserId());
                writeToSocket("[GOODBYE] Client Disconnected!");
            }
        }
    }

    private boolean loginDatabase(Login login){
        try {
            UserData userData = databaseManager.checkUserLogin(login.getUsername(), login.getPassword());
            if (userData != null) {
                changeUserStatus(userData.getUserID());
                userInfo.setUsername(login.getUsername());
                oos.writeObject(new Data("Login validado com sucesso!", userData));
            }
            else
                oos.writeObject(new Data("Login invalido!"));
            oos.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Login IOException");
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerDatabase(Register register){
        try {
            UserData userData = databaseManager.insertUser(register.getName(), register.getUsername(), register.getPassword());
            if (userData != null) {
                userInfo.setUsername(register.getUsername());
                oos.writeObject(new Data("Registo efetuado com sucesso",userData));
            } else
                oos.writeObject(new Data("Registo invalido."));
            oos.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Register IOException");
            e.printStackTrace();
            return false;
        }
    }

    private void processNotification(Notification notification) {
        //todo: Verificar se cliente pertence a este servidor, caso pertença, não precisa avisar outros servidores
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
