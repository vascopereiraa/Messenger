package pt.isec.pd_g33.serverapi.controllers;

import com.sun.tools.jconsole.JConsoleContext;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd_g33.serverapi.database.DatabaseManager;

@RestController
@RequestMapping("list")
public class ListController {

    @GetMapping("groups")
    public String getListGroups(@RequestHeader(name="Authorization") String username) {
        DatabaseManager dbManager = new DatabaseManager();
        String groups = dbManager.listGroups(username);

        return groups;
    }

    @GetMapping("contacts")
    public String getContacts(@RequestHeader(name="Authorization") String token) {
        DatabaseManager dbManager = new DatabaseManager();
        String contacts = dbManager.listContacts(token);


        return contacts;
    }

    @GetMapping("messages/contact/{id}")
    public String getMessagesFromContact(@PathVariable(value = "id") int contactId, @RequestHeader(name="Authorization") String username) {
        DatabaseManager dbManager = new DatabaseManager();
        String contactMessages = dbManager.getContactMsg(username, contactId);

        return contactMessages;
    }

    @GetMapping("messages/group/{id}")
    public String getMessagesFromGroup(@PathVariable(value = "id") int groupId, @RequestHeader(name="Authorization") String username) {
        DatabaseManager dbManager = new DatabaseManager();
        String groupMessages = dbManager.getGroupMsg(username, groupId);

        return groupMessages;
    }
}
