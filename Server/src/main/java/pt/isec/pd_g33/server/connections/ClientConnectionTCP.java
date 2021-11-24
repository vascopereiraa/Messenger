package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Register;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionTCP implements Runnable {

    private Socket sCli;

    public ClientConnectionTCP(Socket scli){
        this.sCli = scli;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());
            Object dataReceived = ois.readObject();

            if(dataReceived instanceof Login) {
                //todo: codigo base dados
            }

            if(dataReceived instanceof Register){
                //todo: codigo base dados
            }

            if(dataReceived instanceof Data){

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
