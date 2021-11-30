package pt.isec.pd_g33.server.data;

import pt.isec.pd_g33.shared.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserInfo {

    private String username;
    private final Socket sCli;
    private ObjectOutputStream oos;

    public UserInfo(Socket sCli) {
        this.sCli = sCli;
        this.oos = oos;
    }

    public String getUsername() {
        return username;
    }

    public void writeSocket(Object o) {
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getSocket(){ return sCli; }
}
