package pt.isec.pd_g33.shared;

import java.io.Serializable;

public class Login implements Serializable {

    private String username;
    private String password;
    private String token;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() { return token; }

    public void setToken(String newToken) { token = newToken; }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
