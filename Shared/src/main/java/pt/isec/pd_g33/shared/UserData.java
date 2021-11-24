package pt.isec.pd_g33.shared;

import java.util.Date;

public class UserData {

    private int userID;
    private String username;
    private String password;
    private String status;
    private String name;
    private Date lastSeen;

    // Em caso de login
    public UserData(String username, String password){
        this.username = username;
        this.password = password;
    }

    // Em caso de registo
    public UserData(String username, String password, String name){
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
