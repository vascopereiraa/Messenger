package pt.isec.pd_g33.serverapi.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd_g33.shared.Login;

@RestController
public class Controller {

    @PostMapping("auth")
    public Login login(@RequestBody Login user) {

    }


}
