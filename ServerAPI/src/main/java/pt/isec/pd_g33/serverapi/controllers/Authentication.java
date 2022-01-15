package pt.isec.pd_g33.serverapi.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd_g33.serverapi.database.DatabaseManager;
import pt.isec.pd_g33.serverapi.models.User;
import java.util.Base64;

@RestController
public class Authentication {

    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

   @PostMapping("auth")
    public User login(@RequestBody User user) {
       DatabaseManager dbManager = new DatabaseManager();
       user.setToken(base64Encoder.encodeToString((user.getUsername()+user.getPassword()).getBytes()));
       return user;
    }
}
