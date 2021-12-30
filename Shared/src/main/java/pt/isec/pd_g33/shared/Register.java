package pt.isec.pd_g33.shared;

import java.io.Serializable;

public class Register implements Serializable {

    private final String username;
    private final String password;
    private final String name;

    public Register(String username, String password, String name) {
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

    public String getName() {
        return name;
    }
}
