package pt.isec.pd_g33.serverapi.controllers;

import org.springframework.web.bind.annotation.*;
import pt.isec.pd_g33.serverapi.database.DatabaseManager;
import pt.isec.pd_g33.shared.Login;

@RestController
@RequestMapping("user")
public class UserController {

    @PutMapping("edit")
    public void editUser(@RequestParam(value = "name") String name, @RequestHeader(name="Authorization") String username) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.editUser(username, name);
        dbManager.close();
    }

    @DeleteMapping("contacts/{name}")
    public String deleteContact(@PathVariable(value = "name") String contactName, @RequestHeader(name="Authorization") String token) {
        DatabaseManager dbManager = new DatabaseManager();
        String msg = dbManager.removeContact(token, contactName);
        DatabaseManager.close();
        return msg;
    }
}
