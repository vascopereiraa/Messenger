package pt.isec.pd_g33.server.database;

import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.UserData;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseManager {
    private final String bdmsLocation;
    private final String username = "root";
    private final String password = "1234";
    private static Connection db;

    public DatabaseManager(String bdmsLocation) {
        this.bdmsLocation = "jdbc:mysql://" + bdmsLocation + "/MessengerDB";
    }

    public static void close() {
        if(db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConnection(){
        try {
            db = DriverManager.getConnection(this.bdmsLocation, username, password);
        } catch (SQLException e) {
            System.err.println("SQLExeption: Ocorreu um erro na conexão a base de dados");
            e.printStackTrace();
        }
    }

    public UserData insertUser(String name, String username, String password) {
        UserData userData;
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "INSERT INTO User(name, username, password, last_seen, status)" +
                    "VALUES('" + name + "','" + username + "','" + password + "','"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "','Online')";
            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);

            long user_id = getUserID(username);
            if(user_id != -1){
                userData = new UserData((int)user_id,username, password, name);
            }else
                return null;
        } catch (SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return null;
        }
        return userData;
    }

    public long getUserID(String username) {
        try (Statement statement = db.createStatement()) {
            String sqlQuery1 = "SELECT user_id FROM User WHERE BINARY username = '" + username + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery1);
            if (resultSet.next()) {
               return resultSet.getLong("user_id");
            } else
                return -1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public String updateUser(String name, String newUsername, String password, int userID) {
        //todo: verificar se ja existe um username igual
        if(getUserID(newUsername) != -1)
            return "Já existe um username com esse nome, coloque outro.";
        try {
            PreparedStatement statement = db.prepareStatement("UPDATE User SET name = ?, username = ?, password =? WHERE user_id =?");
            statement.setString(1, name);
            statement.setString(2, newUsername);
            statement.setString(3, password);
            statement.setInt(4, userID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLException: Erro no update do user.");
            e.printStackTrace();
            return "Ocorreu um erro a atualizar o utilizador";
        }
        return "Utilizador atualizado com sucesso";
    }

    public String insertContact(String fromUserId, String toUserId){
        try (Statement statement = db.createStatement()) {
           String sqlQuery = "INSERT INTO Contact(from_user_id, to_user_id, request_state) VALUES('"+
                   getUserID(fromUserId) + "','" + getUserID(toUserId) + "','pending')";
            statement.executeUpdate(sqlQuery);
            statement.close();
            return "Pedido de contacto enviado com sucesso\n";
        } catch (SQLException e) {
            System.err.println("SQLException: Erro a inserir contacto");
            e.printStackTrace();
        }
        return "Ocorreu um erro a enviar pedido de contacto! Certifique-se que o utilizador e existe e é um novo pedido!\n";
    }

    public UserData checkUserLogin(String username, String password){
        UserData userData;
        try {

            Statement statement = db.createStatement();
            String sqlQuery = "SELECT user_id,username,password,name FROM User WHERE BINARY username = '" + username + "' AND BINARY password ='" + password + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            if(resultSet.next()){
                userData = new UserData(resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("name"));
            } else
                return null;

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return userData;
    }

    public boolean changeUserStatus(int userId) {
        try {
            String status = null;
            String sqlQuery = """
                SELECT status
                FROM User
                WHERE user_id = %d;
                """.formatted(userId);
            Statement statement = db.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(resultSet.next()) {
                status = resultSet.getString("status");
            }

            System.out.println(status);
            assert status != null;
            status = status.equalsIgnoreCase("online") ? "Offline" : "Online";
            sqlQuery = """
                    UPDATE User
                    Set status = '%s'
                    WHERE user_id = %d
                    """.formatted(status, userId);
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String searchUserByName(String user) {
        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT user_id,name, username, last_seen, " +
                    "status FROM User WHERE name LIKE '%" + user + "%'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não existe esse nome!";
            } else {
                sb.append("Encontrou os seguintes users: \n");
                do {
                    sb.append("Nome: ").append(resultSet.getString("name"))
                      .append("\tUsername:").append(resultSet.getString("username"))
                      .append("\tStatus: ").append(resultSet.getString("status")).append("\n");
                } while (resultSet.next());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sb.toString();
    }

    public String listUsers() {
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                SELECT name, username, status, last_seen
                FROM `User`;
                """;
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            StringBuilder sb = new StringBuilder();
            while(resultSet.next()) {
                sb.append("| %-50s | %-30s | %-10s | %-30s |%n".formatted(
                        resultSet.getString("name"),
                        resultSet.getString("username"),
                        resultSet.getString("status"),
                        resultSet.getString("last_seen")));
            }
            if(sb.isEmpty())
                return "[ATTENTION] There is no users registered to list\n";
            String header = "| %-50s | %-30s | %-10s | %-30s |%n".formatted(
                    "Name", "Username", "Status", "Last Seen");
            header = header + "| " + "-".repeat(50) + " | " + "-".repeat(30) + " | " + "-".repeat(10) + " | " + "-".repeat(30) + " |\n";
            return header + sb;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String listContacts(int user_id) {

        StringBuilder sb = new StringBuilder();
        try {

            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT u1.username,u1.name,u1.status,u1.last_seen
                    FROM User u1, Contact c1
                    WHERE c1.to_user_id = u1.user_id
                    AND c1.from_user_id = %d
                    AND c1.request_state LIKE '%%approved%%'
                    UNION
                    SELECT u2.username,u2.name,u2.status,u2.last_seen
                    FROM User u2, Contact c2
                    WHERE c2.from_user_id = u2.user_id
                    AND c2.to_user_id = %d
                    AND c2.request_state LIKE '%%approved%%';     
                    """.formatted(user_id, user_id);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem contactos! Faça amigos novos!";
            } else {
                do {
                    sb.append("Utilizador: Nome:").append(resultSet.getString("name"))
                            .append("\tUsername: ").append(resultSet.getString("username"))
                            .append("\tStatus: ").append(resultSet.getString("status"))
                            .append("\tLast seen: ").append(resultSet.getString("last_seen")).append("\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLExeption: listContacts";
        }
        return sb.toString();
    }

    public boolean isGroupMember(String username_member, int group_id){
        String sqlQuery = """
                        SELECT user_id
                        FROM Participate
                        WHERE user_id = %d
                        AND group_id = %d
                        AND membership_state LIKE '%%approved%%'
                        """.formatted(getUserID(username_member), group_id);
        try (Statement statement = db.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next())
                return true;
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isContact(String from_username, String toUsername){
        String data = listContacts((int)getUserID(from_username));
        if(data.contains(toUsername))
            return true;
        return false;
    }

    public String getUsernameById(int user_id){
        try (Statement statement = db.createStatement()){
            String sqlQuery = "SELECT username FROM User WHERE user_id = '" + user_id + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next()) {
                return resultSet.getString("username");
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getGroupNameById(int group_id){
        try (Statement statement = db.createStatement()){
            String sqlQuery = """
                        SELECT group_name
                        FROM `Group`
                        WHERE group_id = %d
                        """.formatted(group_id);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next()) {
                return resultSet.getString("group_name");
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addMessageToUser(Data data) {
        if(!isContact(data.getToUserUsername(),data.getUserData().getUsername()))
            return false;
        String sqlQuery = """
                INSERT INTO Data(read_state, sent_date, from_user_id, to_user_id, data_type, content)
                VALUES('pending', NOW(), %d, %d, '%s', '%s');
                """.formatted(data.getUserData().getUserID(), getUserID(data.getToUserUsername()), data.getDataType(), data.getContent());
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addMessageToGroup(Data data) {
        if(!isGroupMember(data.getUserData().getUsername(),data.getToGroupId())){
            System.out.println("Não é membro do grupo: " + data.getUserData().getUsername()+" : " +data.getToGroupId());
            return false;
        }
        String sqlQuery = """
                INSERT INTO Data(read_state, sent_date, from_user_id, to_group_id, data_type, content)
                VALUES('pending', NOW(), %d, %d, '%s', '%s');
                """.formatted(data.getUserData().getUserID(), data.getToGroupId(), data.getDataType(), data.getContent());
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String pendingContact(String username) {

        System.out.println("PENDING CONTACT USERNAME: " + username);
        System.out.println("PENDING CONTACT USERNAME ID: " + getUserID(username));

        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        SELECT *
                        FROM Contact
                        WHERE to_user_id = %d
                        AND request_state LIKE '%%pending%%'
                        """.formatted(getUserID(username));
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(!resultSet.next())
                return "Não tem pedidos de contacto pendente";
            else {
                do {
                    sb.append("Request from ").append(getUsernameById(resultSet.getInt("from_user_id"))).append(" is still pending.\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLException pendContact");
            e.printStackTrace();
            return "SQLException: pendContact";
        }
        return sb.toString();
    }

    public String acceptRejectContact(String fromUsername, String toUsername,String acceptReject) {
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        SELECT *
                        FROM Contact
                        WHERE to_user_id = %d
                        AND from_user_id = %d
                        AND request_state LIKE '%%pending%%'
                        """.formatted(getUserID(toUsername),getUserID(fromUsername));
            System.out.println("toUsername: " + getUserID(toUsername) +" fromUsername: " + getUserID(fromUsername));
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(!resultSet.next())
                return "Não tem nenhum pedido de contacto pendendo do " + fromUsername;
            else {
                // Caso exista o pedido, vai ser aceite ou eliminado
                acceptOrRejectUpdate(fromUsername, toUsername, acceptReject);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLException: acceptRejectContact";
        }
        return "Contacto aceite com sucesso. " + fromUsername + " pertence agora a sua lista de contactos.";
    }

    public void acceptOrRejectUpdate(String fromUsername, String toUsername, String acceptReject){
        try {
            if (acceptReject.equalsIgnoreCase("accept")) {
                PreparedStatement prepStatement = db.prepareStatement(
                        "UPDATE Contact SET request_state = ? WHERE to_user_id =? AND from_user_id =?");
                prepStatement.setString(1, "approved");
                prepStatement.setInt(2, (int) getUserID(toUsername));
                prepStatement.setInt(3, (int) getUserID(fromUsername));
                prepStatement.executeUpdate();
                prepStatement.close();
            } else { // Caso contrario, o pedido vai ser eliminado da BD
                Statement statement = db.createStatement();
                String sqlQuery = """
                        DELETE FROM Contact
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND request_state LIKE '%%pending%%'
                        """.formatted(getUserID(toUsername), getUserID(fromUsername));
                statement.executeUpdate(sqlQuery);
                statement.close();
            }
        } catch (SQLException e) {
            System.err.println("SQLException: acceptOrRejectUpdate");
            e.printStackTrace();
        }
    }

    public String addNewGroup(String groupName, int adminUserId){
        // Verificar se o utilizador ja tem um grupo com o mesmo nome
        if(!adminHasGroupname(groupName,adminUserId))
            return "O utilizador já administrador de um grupo com o mesmo nome! Escolha outro nome.\n";
        String sqlQuery = """
                INSERT INTO `Group` (group_name, admin_user_id)
                VALUES('%s', %d);
                """.formatted(groupName, adminUserId);
        // Juntar logo ao grupo o criador
        joinGroup(adminUserId,getGroupIdByNameAndAdminID(groupName,adminUserId),"approved");
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ocorreu um erro. Não foi possível criar um novo grupo";
        }
        return "Novo grupo " + groupName + " criado";
    }

    public boolean adminHasGroupname(String groupName, int adminUserId){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                        SELECT *
                        FROM `Group`
                        WHERE BINARY group_name = '%s'
                        AND admin_user_id = %d
                        """.formatted(groupName,adminUserId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next())
                return false;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getGroupIdByNameAndAdminID(String groupName,int adminID){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                        SELECT group_id
                        FROM `Group`
                        WHERE BINARY group_name = '%s'
                        AND admin_user_id = %d
                        """.formatted(groupName,adminID);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next())
                return resultSet.getInt("group_id");
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean joinGroup(int userId, int groupId, String status){
        String sqlQuery = """
                   INSERT INTO Participate(user_id, group_id, membership_state)
                   VALUES(%d, %d, '%s');
                   """.formatted(userId, groupId, status);
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptOrRejectGroupMember(int groupId, String memberUsername, String acceptReject,String groupAdmin){
        if(!getGroupAdmin(groupId).equals(groupAdmin))
            return false;

        try {
            PreparedStatement prepStatement = null;
            if (acceptReject.equalsIgnoreCase("accept")) {
                prepStatement = db.prepareStatement("UPDATE Participate SET membership_state = ? WHERE user_id =? AND group_id =?");
                prepStatement.setString(1, "approved");
                prepStatement.setInt(2, (int) getUserID(memberUsername));
                prepStatement.setInt(3, groupId);
                prepStatement.executeUpdate();
                prepStatement.close();
                return true;
            }
            else if(deleteParticipateMember((int)getUserID(memberUsername), groupId, "pending")) return true;
        } catch (SQLException e) {
            System.err.println("SQLException: acceptOrRejectGroupMember");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String updateGroupName(String groupName, int groupId, String groupAdmin){
        if(!getGroupAdmin(groupId).equals(groupAdmin))
            return "Indique por favor um grupo em que seja administrador.";
        String sqlQuery = """
                    UPDATE `Group`
                    Set group_name = '%s'
                    WHERE group_id = %d
                    """.formatted(groupName, groupId);
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
            statement.close();
            return "Nome do grupo atualizado\n";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ocorreu um erro. Não foi possível atualizar o nome do grupo\n";
        }
    }

    public String getGroupAdmin(int groupId){
        try (Statement statement = db.createStatement()){
            String sqlQuery = """ 
                                  SELECT u.username
                                  FROM User u, `Group` g
                                  WHERE u.user_id = g.admin_user_id
                                  AND g.group_id = %d;
                                  """.formatted(groupId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next()) {
                return resultSet.getString("username");
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String leaveGroup(UserData member, int groupId, String membershipState){
        System.out.println(getGroupAdmin(groupId));
        System.out.println(member.getUsername());
        if(getGroupAdmin(groupId).equals(member.getUsername())){
            return deleteGroup(member.getUsername(), groupId);
        }
        if(deleteParticipateMember((int)member.getUserID(), groupId, "approved")){
            return "Deixou de fazer parte do grupo";
        }
        return "Não é possível abandonar o grupo. Gostam demasiado de ti!!! Não faça isto!!!";
    }

    public boolean deleteParticipateMember(int userId, int groupId, String membershipState){
        try {
            PreparedStatement prepStatement = db.prepareStatement("DELETE FROM Participate WHERE user_id = ? AND group_id = ?  AND membership_state = ?");
            prepStatement.setInt(1, userId);
            prepStatement.setInt(2, groupId);
            prepStatement.setString(3,membershipState);
            if(prepStatement.executeUpdate() == 1){
                return true;
            }
            prepStatement.close();
        } catch (SQLException e) {
            System.err.println("SQLException: deleteParticipateMember");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String deleteContact(String fromUsername, String toUsername) {
        if (isContact(fromUsername, toUsername)) {
            try {
                Statement statement = db.createStatement();
                String sqlQuery = """ 
                        DELETE FROM Contact
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND from_user_id = %d OR to_user_id = %d
                        AND request_state LIKE '%%approved%%'
                        """.formatted(getUserID(fromUsername), getUserID(fromUsername), getUserID(toUsername), getUserID(toUsername));
                if (statement.executeUpdate(sqlQuery) == 2) {

                    statement.close();
                    return "Não foi possivel eliminar o contacto " + toUsername;
                }
            } catch (SQLException e) {
                System.err.println("SQLExeption deleteContact");
                e.printStackTrace();
                return "SQLExeption deleteContact";
            }
            deleteMsgsAndFiles(getUserID(fromUsername), getUserID(toUsername));
            return "Contacto Eliminado com sucesso! ";
        }
        return "Esse contacto não existe na sua lista de contactos!\n";
    }

    public void deleteMsgsAndFiles(long fromUserId, long toUserId){
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        DELETE FROM Data
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND from_user_id = %d OR to_user_id = %d
                        AND data_type LIKE '%%message%%' 
                        OR data_type LIKE '%%file%%' 
                        """.formatted(fromUserId, fromUserId, toUserId, toUserId);
            statement.executeUpdate(sqlQuery);
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteContact");
            e.printStackTrace();
        }
    }

    public String listUserMsg(String fromUsername, String toUserName){
        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT d1.from_user_id,d1.to_user_id, d1.content, d1.read_state
                    FROM `Data` d1
                    WHERE d1.to_user_id = %d AND d1.from_user_id = %d
                    UNION
                    SELECT d2.from_user_id,d2.to_user_id, d2.content, d2.read_state
                    FROM `Data` d2
                    WHERE d2.from_user_id = %d AND d2.to_user_id = %d;
                    """.formatted(getUserID(toUserName), getUserID(fromUsername),
                                  getUserID(fromUsername), getUserID(toUserName));
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem mensagens com o " + toUserName;
            } else {
                do {
                    sb.append("O [").append(getUsernameById(resultSet.getInt("from_user_id")))
                            .append("] enviou uma mensagem a [").append(getUsernameById(resultSet.getInt("to_user_id")))
                            .append("]\tRead State: ").append(resultSet.getString("read_state"))
                            .append("\n\tMensagem:").append(resultSet.getString("content")).append("\n\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLExeption: listUserMsg";
        }
        return sb.toString();
    }

    public boolean belongsToGroup(String fromUsername,int groupId){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                    SELECT *
                    FROM Participate
                    WHERE user_id = %d
                    AND group_id = %d
                    AND membership_state LIKE '%%approved%%';
                    """.formatted(getUserID(fromUsername), groupId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next())
                return true;
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String listGroupMsg(String fromUsername, int groupId){
        if(!belongsToGroup(fromUsername,groupId))
            return "Indique por favor um grupo a que pertença.";

        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT from_user_id, content, read_state
                    FROM Data 
                    WHERE to_user_id = %d OR from_user_id = %d
                    """.formatted(getUserID(fromUsername), groupId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem mensagens com o grupo " + getGroupNameById(groupId);
            } else {
                do {
                    sb.append("O [").append(getUsernameById(resultSet.getInt("from_user_id")))
                            .append("] enviou uma mensagem para o grupo [").append(getGroupNameById(groupId))
                            .append("\tRead State: ").append(resultSet.getString("read_state"))
                            .append("\nMensagem: ").append(resultSet.getString("content")).append("\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLExeption: listGroupMsg";
        }
        return sb.toString();
    }

    public String listGroups() {
        String sqlQuery = """
                SELECT g.group_id, group_name, u.username
                FROM `Group` g, User u
                WHERE g.admin_user_id = u.user_id;
                """;
        try (Statement statement = db.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            StringBuilder sb = new StringBuilder();
            while(resultSet.next()) {
                int group_id = resultSet.getInt("group_id");
                sb.append("| %-5s | %-30s | %-30s | %-50s | %-30s | %-10s |%n".formatted(
                        group_id,
                        resultSet.getString("group_name"),
                        resultSet.getString("username"),
                        " " ,
                        " ",
                        " "));

                String memberQuery = """
                        SELECT u.name, u.username, u.status
                        FROM User u, Participate p
                        WHERE u.user_id = p.user_id
                        AND p.group_id = %d;""".formatted(group_id);
                Statement newQuery = db.createStatement();
                ResultSet membersList = newQuery.executeQuery(memberQuery);
                while(membersList.next())
                    sb.append("| %-5s | %-30s | %-30s | %-50s | %-30s | %-10s |%n".formatted(
                            " ",
                            " ",
                            " ",
                            membersList.getString("name"),
                            membersList.getString("username"),
                            membersList.getString("status")
                    ));
                newQuery.close();
            }
            String header = "| %-5s | %-30s | %-30s | %-50s | %-30s | %-10s |%n".formatted(
                    "ID", "Group Name", "Admin", "Members: Name", "Username", "Status");
            header = header + "| " + "-".repeat(5) + " | " + "-".repeat(30) + " | " + "-".repeat(30) + " | "
                    + "-".repeat(50) + " | " + "-".repeat(30) + " | " + "-".repeat(10) + " |\n";
            return header + sb;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String deleteMsg(String username, int messageID) {
        String sqlQuery = """
                SELECT *
                FROM `Data`
                WHERE data_id = %d
                AND data_type LIKE '%%message%%'
                """.formatted(messageID);
        try (Statement statement = db.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (resultSet.next()) {
                if (resultSet.getInt("from_user_id") == getUserID(username)) {
                    if (removeMsg(messageID))
                        return "Mensagem apagada com sucesso!";
                    return "Ocorreu um erro a tentar eliminar a mensagem";
                }
            }
            return "Não existe a mensagem com esse ID";
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLException: deleteMsg";
        }
    }

    public boolean removeMsg(int messageID){
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        DELETE FROM `Data`
                        WHERE data_id = %d
                        """.formatted(messageID);
            statement.executeUpdate(sqlQuery);
            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println("SQLException: deleteContact");
            e.printStackTrace();
            return false;
        }
    }


    public String listUnseen(String username) {
        StringBuilder sb = new StringBuilder("Listagem de todas as mensagens não vistas:\n\n");
        String sqlQuery = """
                SELECT data_id,from_user_id,from_user_id, content, to_group_id
                FROM `Data`
                WHERE to_user_id = %d
                AND read_state LIKE '%%unseen%%'
                UNION
                SELECT data_id,from_user_id,from_user_id, content, to_group_id
                FROM `Data`
                WHERE to_group_id IS NOT NULL
                AND read_state LIKE '%%unseen%%'
                """.formatted(getUserID(username));
        try (Statement statement = db.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem mensagens por ver!\n\n";
            } else {
                do {
                    int groupID = resultSet.getInt("to_group_id");
                    // Caso seja uma mensagem para um grupo, é necessário verificar se o user pertence a esse grupo
                    if (groupID != 0){
                        if (belongsToGroup(username, groupID)){
                            sb.append("Mensagem por ver do grupo: ").append(getGroupNameById(groupID));
                            if(!updateReadState(resultSet.getInt("data_id")))
                                return "Ocorreu um erro a atualizar o estado de leitura";
                        }
                    } else {
                        sb.append("Mensagem por ver do utilizador: ").append(getUsernameById(resultSet.getInt("from_user_id")));
                        if(!updateReadState(resultSet.getInt("data_id")))
                            return "Ocorreu um erro a atualizar o estado de leitura";
                    }
                    sb.append("\tData id: ").append(resultSet.getInt("data_id"));
                    sb.append("\n\tMensagem .: ").append(resultSet.getString("content")).append("\n");
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLException: listUnseen";
        }
        return sb.toString();
    }

    public boolean updateReadState(int dataId){
        try {
            PreparedStatement prepStatement = db.prepareStatement("UPDATE `Data` SET read_state = ? WHERE data_id = ?");
            prepStatement.setString(1,"seen");
            prepStatement.setInt(2, dataId);
            prepStatement.executeUpdate();
            prepStatement.close();
            return true;
        } catch (SQLException e) {
            System.err.println("SQLException: updateReadState");
            e.printStackTrace();
            return false;
        }
    }

    public String deleteGroup(String username, int groupID) {
        String groupName =  getGroupNameById(groupID);
        if(!getGroupAdmin(groupID).equals(username))
            return "Tem que ser administrador do grupo para o poder eliminar!\n";
        try {
            if(!deleteAllGroupMembers(groupID))
                return "Ocorreu um erro a eliminar os membros do grupo excluido " + getGroupNameById(groupID) +"\n";
            if(!deleteGroupMsgs(groupID))
                return "Ocorreu um erro a eliminar todas as mensagens referente ao grupo\n";
            Statement statement = db.createStatement();
            String sqlQuery = """ 
                        DELETE FROM `Group`
                        WHERE group_id = %d
                        """.formatted(groupID);
            if (statement.executeUpdate(sqlQuery) == 2) {
                statement.close();
                return "Não foi possivel eliminar o grupo " + getGroupNameById(groupID);
            }
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteGroup");
            e.printStackTrace();
            return "SQLExeption deleteGroup";
        }
        return "O grupo " + groupName + " foi eliminado com sucesso, os seus membros removidos bem como o seu histórico de mensagens!\n";
    }

    public boolean deleteAllGroupMembers(int groupID){
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """ 
                        DELETE FROM `Participate`
                        WHERE group_id = %d
                        """.formatted(groupID);
            if (statement.executeUpdate(sqlQuery) == 2) {
                statement.close();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteGroupMembers");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteGroupMsgs(int groupID) {
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        DELETE FROM `Data`
                        WHERE to_group_id = %d
                        """.formatted(groupID);
            statement.executeUpdate(sqlQuery);
            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println("SQLException: deleteContact");
            e.printStackTrace();
            return false;
        }
    }

}
