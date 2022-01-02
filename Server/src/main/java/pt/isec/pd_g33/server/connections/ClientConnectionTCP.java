package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.server.file.ThreadReceiveFiles;
import pt.isec.pd_g33.shared.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

// Thread criada para cada ligação TCP entre servidor clientes
public class ClientConnectionTCP implements Runnable {

    private static final int UNICAST_NOTIFICATION_PORT = 2000;
    private final DatabaseManager databaseManager;

    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final int portToReceiveFiles;
    private final String ipToReceiveFiles;

    private Object dataReceived;
    private final UserInfo userInfo;
    private final List<UserInfo> listUsers;

    private final String folderPath;

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
                //e.printStackTrace();
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
        try {
            switch (dataReceived.getMenuOptionSelected()) {
                // User
                case EDIT_USER -> writeToSocket(databaseManager.updateUser(dataReceived.getUserData().getName(), dataReceived.getUserData().getUsername(),
                        dataReceived.getUserData().getPassword(), dataReceived.getToUserId()));
                case LIST_USERS -> writeToSocket(databaseManager.listUsers());
                case SEARCH_USER -> writeToSocket(databaseManager.searchUserByName(dataReceived.getContent()));

                // Groups
                case CREATE_GROUP -> writeToSocket(databaseManager.addNewGroup(dataReceived.getContent(), dataReceived.getUserData().getUserID()));
                case LIST_GROUPS -> writeToSocket(databaseManager.listGroups());
                case JOIN_GROUP -> {
                    if (databaseManager.joinGroup(dataReceived.getUserData().getUserID(), dataReceived.getToUserId(), "pending")) {
                        writeToSocket("Foi enviado um pedido de adesão ao administrador do grupo");
                        processNotification(new Notification(dataReceived.getUserData().getUsername(), databaseManager.getGroupAdmin(dataReceived.getToUserId()), DataType.Group));
                    } else
                        writeToSocket("Ocorreu um erro. Não foi possível aderir a grupo. Certifique-se que o grupo existe e que já não pertence a ele.");
                }
                case MEMBER_ACCEPT -> {
                    if (databaseManager.acceptOrRejectGroupMember(dataReceived.getToGroupId(), dataReceived.getContent(), "accept", dataReceived.getUserData().getUsername(),false)) {
                        writeToSocket(dataReceived.getContent().trim() + " foi adicionado ao grupo.");
                        processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getContent(), DataType.Group, dataReceived.getToGroupId(),
                                databaseManager.getGroupNameById(dataReceived.getToGroupId()), "aceite"));
                    } else
                        writeToSocket("Ocorreu um erro. Não foi possível aceitar um novo membro no grupo! Certifique-se que tem os dados corretos, o membro existe e está pendente e é administrador!");
                }
                case MEMBER_REMOVE -> {
                    if (databaseManager.acceptOrRejectGroupMember(dataReceived.getToGroupId(), dataReceived.getContent(), "reject", dataReceived.getUserData().getUsername(),true)) {
                        writeToSocket(dataReceived.getContent().trim() + " foi removido do grupo.");
                        processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getContent(), DataType.Group, dataReceived.getToGroupId(),
                                databaseManager.getGroupNameById(dataReceived.getToGroupId()), "removido"));
                    } else
                        writeToSocket("Ocorreu um erro. Não foi possível fazer o remove do membro no grupo! Certifique-se que tem os dados corretos, o membro existe e está aceite e é administrador!");
                }
                case RENAME_GROUP -> writeToSocket(databaseManager.updateGroupName(dataReceived.getContent(), dataReceived.getToGroupId(), dataReceived.getUserData().getUsername()));
                case DELETE_GROUP -> writeToSocket(databaseManager.deleteGroup(dataReceived.getContent(), dataReceived.getToGroupId()));
                case LEAVE_GROUP -> writeToSocket(databaseManager.leaveGroup(dataReceived.getUserData(), dataReceived.getToUserId()));

                // Contacts
                case LIST_CONTACTS -> writeToSocket(databaseManager.listContacts((int) databaseManager.getUserID(userInfo.getUsername())));
                case PENDING_CONTACT -> writeToSocket(databaseManager.pendingContact(dataReceived.getContent()));
                case DELETE_CONTACT -> writeToSocket(databaseManager.deleteContact(dataReceived.getUserData().getUsername(), dataReceived.getContent()));
                case ACCEPT_CONTACT -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(), dataReceived.getContent(), "accept"));
                case REJECT_CONTACT -> writeToSocket(databaseManager.acceptRejectContact(dataReceived.getToUserUsername(), dataReceived.getContent(), "reject"));
                case ADD_CONTACT -> {
                    String a = databaseManager.insertContact(dataReceived.getContent(), dataReceived.getToUserUsername());
                    writeToSocket(a);
                    if(!a.contains("Não é possivel adicionar-se a si mesmo como contacto"))
                        processNotification(new Notification(dataReceived.getContent(), dataReceived.getToUserUsername(), DataType.Contact));
                }
                // Messages
                case SEND_MSG_TO_CONTACT -> {
                    if (databaseManager.addMsgAndFilesUsers(dataReceived)) {
                        writeToSocket("[SUCCESS] Message sent successfully to " + dataReceived.getToUserUsername());
                        processNotification(new Notification(dataReceived.getUserData().getUsername(),
                                dataReceived.getToUserUsername(), dataReceived.getDataType()));
                    } else
                        writeToSocket("[WARNING] " + dataReceived.getToUserUsername() + " does not make part of your contacts list");
                }
                case SEND_MSG_TO_GROUP -> {
                    if (databaseManager.addMsgAndFilesGroups(dataReceived)) {
                        writeToSocket("[SUCCESS] Message sent successfully to " + databaseManager.getGroupNameById(dataReceived.getToGroupId()));

                        ArrayList<String> arrayOfUsernames = databaseManager.getArraylistOfGroupMembers(dataReceived.getToGroupId());
                        for (String toUsername : arrayOfUsernames)
                            if(!toUsername.equals(userInfo.getUsername()))
                                processNotification(new Notification(dataReceived.getUserData().getUsername(),
                                        toUsername,dataReceived.getToGroupId(),databaseManager.getGroupNameById(dataReceived.getToGroupId()) ,dataReceived.getDataType()));
                    } else
                        writeToSocket("[WARNING] You are not a member of this group!");
                }
                case LIST_MSG_FILES_CONTACT -> writeToSocket(databaseManager.listUserMsg(dataReceived.getContent(), dataReceived.getToUserUsername()));
                case LIST_MSG_FILES_GROUP -> writeToSocket(databaseManager.listGroupMsg(dataReceived.getContent(), Integer.parseInt(dataReceived.getToUserUsername())));
                case LIST_UNSEEN -> writeToSocket(databaseManager.listUnseen(dataReceived.getContent()));
                case DELETE_MESSAGE -> writeToSocket(databaseManager.deleteMsg(dataReceived.getContent(), dataReceived.getToGroupId() /* groupid == MSG id */));

                // Files
                case SEND_FILE_TO_CONTACT -> {
                    if (databaseManager.addMsgAndFilesUsers(dataReceived)) {
                        // Obtem o fileID para atualizar o nome
                        int fileID = databaseManager.getFileIDFromUser(dataReceived);
                        String[] filenameSplit = dataReceived.getContent().split("\\.");
                        dataReceived.setContent(filenameSplit[0] + fileID + '.' + filenameSplit[1]);
                        // Atualiza na BD o nome do ficheiro
                        databaseManager.updateFileName(fileID, dataReceived.getContent());

                        ThreadReceiveFiles threadReceiveFiles = new ThreadReceiveFiles(dataReceived.getReadState(), // Ip
                                dataReceived.getToUserId(), // Porto
                                folderPath, dataReceived.getContent()); // ID da entry do File na BD
                        Thread received = new Thread(threadReceiveFiles);
                        received.start();

                        writeToSocket("[SUCCESS] File sent successfully to " + dataReceived.getToUserUsername());
                        processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getToUserUsername(), databaseManager.getFileIDFromGroup(dataReceived),
                                DataType.File, dataReceived.getContent() + " received!", ipToReceiveFiles, portToReceiveFiles));
                    } else {
                        writeToSocket("[WARNING] " + dataReceived.getToUserUsername() + " does not make part of your contacts list");
                    }
                }
                case SEND_FILE_TO_GROUP -> {
                    if (databaseManager.addMsgAndFilesGroups(dataReceived)) {
                        // Obtem o fileID para atualizar o nome
                        int fileID = databaseManager.getFileIDFromGroup(dataReceived);
                        String[] filenameSplit = dataReceived.getContent().split("\\.");
                        dataReceived.setContent(filenameSplit[0] + fileID + '.' + filenameSplit[1]);
                        // Atualiza na BD o nome do ficheiro
                        databaseManager.updateFileName(fileID, dataReceived.getContent());

                        writeToSocket("[SUCCESS] File sent successfully to group id " + dataReceived.getToGroupId());
                        ThreadReceiveFiles threadReceiveFiles = new ThreadReceiveFiles(dataReceived.getReadState(), // Ip
                                dataReceived.getToUserId(), // Porto
                                folderPath, dataReceived.getContent()); // Filename
                        Thread received = new Thread(threadReceiveFiles);
                        received.start();

                        ArrayList<String> arrayOfUsernames = databaseManager.getArraylistOfGroupMembers(dataReceived.getToGroupId());
                        for (String toUsername : arrayOfUsernames)
                            if(!toUsername.equals(userInfo.getUsername()))
                                processNotification(new Notification(dataReceived.getUserData().getUsername(), dataReceived.getToGroupId(), databaseManager.getUsernameById(dataReceived.getToGroupId()),
                                        toUsername, DataType.File, dataReceived.getContent(), ipToReceiveFiles, portToReceiveFiles, databaseManager.getFileIDFromGroup(dataReceived)));

                    } else {
                        writeToSocket("[WARNING] " + dataReceived.getToUserUsername() + " does not make part of your contacts list");
                    }
                }
                case REQUEST_FILE_FROM_CONTACT -> {
                    if (new File(folderPath + File.separator + dataReceived.getContent()).exists()) { // Ficheiro existe no armaz. do SV
                        if (databaseManager.isFileToContact(dataReceived.getContent(), dataReceived.getToUserUsername(), dataReceived.getUserData().getUsername())) {
                            dataReceived.setReadState(ipToReceiveFiles);
                            dataReceived.setToUserId(portToReceiveFiles);
                            writeToSocket(dataReceived);
                        } else
                            writeToSocket("[ERROR] Don't have any file from this contact with that filename");
                    } else
                        writeToSocket("[ERROR] Don't exist a file with that filename");
                }
                case REQUEST_FILE_FROM_GROUP -> {

                }
                case DELETE_FILE -> {
                    File file = new File(folderPath + File.separator + dataReceived.getContent());
                    if (file.exists()) { // Ficheiro existe no armaz. do SV
                        String resultado = databaseManager.deleteFileFromUser(dataReceived.getToGroupId(), dataReceived.getContent());
                        writeToSocket(resultado);
                        if (resultado.contains("sucesso")) { // Caso o ficheiro exista, vai ser apagado do alojamento local
                            file.delete();
                            processNotification(new Notification(dataReceived.getContent(), "deletefile", DataType.File));
                        }
                    }
                }
                // Recebe heartbeat para colocar cliente online
                case SET_ONLINE -> {
                    //System.out.println("Recebi heartbeat do: " + dataReceived.getUserData().getUsername());
                    databaseManager.setClientOnline(dataReceived.getUserData());
                }
                // Exit
                case EXIT -> {
                    changeUserStatus(dataReceived.getToUserId(), 0);
                    writeToSocket("[GOODBYE] Client Disconnected!");
                }
            }
        }catch (Exception e ){
            System.out.println("Jogador já não existe.");
        }
    }

    private boolean loginDatabase(Login login){
        try {
            UserData userData = databaseManager.checkUserLogin(login.getUsername(), login.getPassword());
            if (userData != null) {
                if (userData.getStatus().equalsIgnoreCase("online")) {
                    oos.writeUnshared(new Data("O utilizador já se encontra com sessão iniciada"));
                } else {
                    changeUserStatus(userData.getUserID(), 1);
                    userInfo.setUsername(login.getUsername());
                    oos.writeObject(new Data("Login validado com sucesso!", userData));
                }
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
                oos.writeObject(new Data("Registo invalido. Tem que ser um novo utilizador."));
            oos.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Register IOException");
            e.printStackTrace();
            return false;
        }
    }

    private void processNotification(Notification notification) {
        // Verificar se cliente pertence a este servidor, caso pertença, não precisa avisar outros servidores
        for(UserInfo u : listUsers) {
            if(u.getUsername()!=null){
                System.out.println("Username: " + u.getUsername() + " to username: " + notification.getToUsername());
                if(u.getUsername().equals(notification.getToUsername())) {
                    u.writeSocket(notification);
                    return;
                }
            }
        }
        // Envia notificacao ao GRDS
        sendNotificationToGRDS(notification);
    }

    private void writeToSocket(Object o) {
        userInfo.writeSocket(o);
    }

    private void changeUserStatus(int userId, int status) {
        databaseManager.changeUserStatus(userId, status);
    }

    public static void sendNotificationToGRDS(Notification notification){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(notification);
            oos.flush();

            DatagramSocket ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName("127.0.0.1"), UNICAST_NOTIFICATION_PORT);
            ds.send(dp);
        } catch (IOException e) {
            System.err.println("IOException: processNotification");
            e.printStackTrace();
        }
    }

}
