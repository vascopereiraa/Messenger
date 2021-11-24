package pt.isec.pd_g33.shared;

import java.io.Serializable;

public class Login implements Serializable {

    private final String username;
    private final String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
