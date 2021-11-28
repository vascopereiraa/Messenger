package pt.isec.pd_g33.server.data;

import pt.isec.pd_g33.shared.Notification;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserInfo {

    private String username;
    private final Socket sCli;

    public UserInfo(Socket sCli) {
        this.sCli = sCli;
    }

    public String getUsername() {
        return username;
    }

    public void writeSocket(Notification notification) {
        synchronized (sCli) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
                oos.writeObject(notification);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
