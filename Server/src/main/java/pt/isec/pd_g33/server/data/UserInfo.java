package pt.isec.pd_g33.server.data;


import java.io.IOException;
import java.io.ObjectOutputStream;

public class UserInfo {

    private String username;
    private final ObjectOutputStream oos;

    public UserInfo(ObjectOutputStream oos) {
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

    @Override
    public String toString() {
        return "UserInfo .: username=" + username + "\n";
    }
}
