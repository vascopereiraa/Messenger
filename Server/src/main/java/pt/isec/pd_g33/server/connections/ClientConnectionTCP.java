package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Register;
import pt.isec.pd_g33.shared.UserData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionTCP implements Runnable {

    private Socket sCli;
    private DatabaseManager databaseManager;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Object dataReceived;

    public ClientConnectionTCP(Socket scli, DatabaseManager databaseManager){
        this.sCli = scli;
        this.databaseManager = databaseManager;
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
        }
    }

    private void processData(Data dataReceived) {

        switch (dataReceived.getMenuOptionSelected()){
            case 1-> {
                if(databaseManager.updateUser(
                        dataReceived.getUserData().getName(),
                        dataReceived.getUserData().getUsername(),
                        dataReceived.getUserData().getPassword(),
                        dataReceived.getToUserId())){
                    writeToSocket("Utilizador atualizado com sucesso !");
                }else
                    writeToSocket("Não foi possível atualizar o utilizador");
            }
            case 2-> {
                writeToSocket(databaseManager.listUsers());
            }
            case 3->{
                writeToSocket(databaseManager.searchUserByName(dataReceived.getContent()));
            }
            case 4->{

            }
            case 5->{
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
            if (userData != null)
                oos.writeObject(new Data("Login validado com sucesso!",userData));
            else
                oos.writeObject(new Data("Login invalido."));
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

    private void writeToSocket(Object o) {
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException e) {
            System.err.println("IOExeption: ");
            e.printStackTrace();
        }
    }
}
