package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.database.DatabaseManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Register;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;

public class ClientConnectionTCP implements Runnable {

    private Socket sCli;
    private DatabaseManager databaseManager;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Object dataReceived;

    public ClientConnectionTCP(Socket scli, DatabaseManager databaseManager){
        this.sCli = scli;
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {
        //todo: Debug
        System.out.println("\nVou listar os users\n");
        databaseManager.listUsers();
        System.out.println("\n");

        try {
            while(true){
                oos = new ObjectOutputStream(sCli.getOutputStream());
                ois = new ObjectInputStream(sCli.getInputStream());
                dataReceived = ois.readObject();

                if(dataReceived instanceof Login) {
                    if(!loginDatabase()) return;
                }
                if(dataReceived instanceof Register){
                    if(!registerDatabase()) return;
                }
                if(dataReceived instanceof Data){
                    processData();
                }
            }
        } catch(SocketException e){
            System.out.println("SocketException: O cliente foi-se embora!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean processData() {

        return true;
    }

    private boolean loginDatabase(){
        try {
            if (databaseManager.checkUserLogin(((Login) dataReceived).getUsername(), ((Login) dataReceived).getPassword())) {
                oos.writeUnshared("Login validado com sucesso.");
                // oos.flush();
            } else {
                oos.writeUnshared("Login invalido.");
                // oos.flush();
            }
        } catch (IOException e) {
            System.err.println("Login IOExecption");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean registerDatabase(){
        try {
            if (databaseManager.insertUser( ((Register) dataReceived).getName(),
                                            ((Register) dataReceived).getUsername(),
                                            ((Register) dataReceived).getPassword())) {
                oos.writeUnshared("Registo validado com sucesso.");
                // oos.flush();
            } else {
                oos.writeUnshared("Registo invalido.");
                // oos.flush();
            }
        } catch (IOException e) {
            System.err.println("Register IOExecption");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
