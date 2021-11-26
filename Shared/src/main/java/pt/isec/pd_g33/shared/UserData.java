package pt.isec.pd_g33.shared;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class UserData implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private int userID;
    private String username;
    private String password;
    private String status;
    private String name;
    private Date lastSeen;

    private ArrayList<UserData> contactos = new ArrayList<>();

    private ArrayList<Data> historicoMsg;       // [SENDER]: msg - DATE -> select name, content where type = message and sender = x and receiver = y or sender = y and receiver = x
    private ArrayList<Data> historicoFicheiro;  // new Data()
    //private ArrayList<Groups> grupos;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
