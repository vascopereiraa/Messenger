package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Notification;
import pt.isec.pd_g33.shared.Register;
import pt.isec.pd_g33.shared.UserData;
import static pt.isec.pd_g33.server.connections.ThreadMessageReflection.REFLECTION_IP;
import static pt.isec.pd_g33.server.connections.ThreadMessageReflection.REFLECTION_PORT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectionTCP implements Runnable {

    private final Socket sCli;
    private final DatabaseManager databaseManager;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Object dataReceived;
    private final UserInfo userInfo;

    public ClientConnectionTCP(Socket scli, DatabaseManager databaseManager, UserInfo userInfo){
        this.sCli = scli;
        this.databaseManager = databaseManager;
        this.userInfo = userInfo;

        //todo: porque é que a conecção com a BD se fecha ?
        this.databaseManager.setConnection();
        try {
            oos = new ObjectOutputStream(sCli.getOutputStream());
            ois = new ObjectInputStream(sCli.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                processNotification(n);
            }
        }
    }

    private void processData(Data dataReceived) {

        switch (dataReceived.getMenuOptionSelected()){
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

            }
            case 5 -> {
                if(databaseManager.deleteUser(dataReceived.getContent())) {
                    writeToSocket("Utilizador eliminado");
                } else {
                    writeToSocket("Não foi possível eliminar o utilizador pretendido.");
                }

            }
            default -> {
                System.err.println("Opção invalidade de menu");
            }
        }
    }

    private boolean loginDatabase(){
        try {
            UserData userData = databaseManager.checkUserLogin(((Login) dataReceived).getUsername(), ((Login) dataReceived).getPassword());
            if (userData != null){
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

    private void processNotification(Notification n) {

        if(n.getDataType() == DataType.Contact)
            databaseManager.insertContact(n.getFromUsername(),n.getToUsername());
        else
        {
            //todo: insertData aqui, para msg's e notificações
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(n);
            oos.flush();

            DatagramSocket ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName(REFLECTION_IP), REFLECTION_PORT);
            ds.send(dp);
        } catch (IOException e) {
            System.err.println("IOException: processNotification");
            e.printStackTrace();
        }
    }

    private void writeToSocket(Object o) {
        synchronized (sCli) {
            try {
                oos.writeObject(o);
                oos.flush();
            } catch (IOException e) {
                System.err.println("IOExeption: WriteToSocket ClientConnectionTCP");
                e.printStackTrace();
            }
        }
    }
}
