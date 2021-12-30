package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;

public class UserData implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private int userID;
    private final String username;
    private final String password;
    private String status;
    private final String name;

    // Em caso de registo
    public UserData(String username, String password, String name){
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public UserData(int user_id, String username, String password, String name){
        this.userID = user_id;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getUserID() {
        return userID;
    }
}
