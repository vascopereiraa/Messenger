package pt.isec.pd_g33.server.database;

import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.UserData;

import javax.xml.transform.Result;
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


    public boolean updateUser(String name, String newUsername, String password, int userID) {
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
            return false;
        }
        return true;
    }

    public boolean insertContact(String fromUserId, String toUserId){
        try (Statement statement = db.createStatement()) {
           String sqlQuery = "INSERT INTO Contact(from_user_id, to_user_id, request_state) VALUES('"+
                   getUserID(fromUserId) + "','" + getUserID(toUserId) + "','pending')";
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            System.err.println("SQLException: Erro a inserir contacto");
            e.printStackTrace();
        return false;
        }
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

    public String searchUserByName(String user) {
        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT user_id,name, username, last_seen, " +
                    "status FROM User WHERE name LIKE '%" + user + "%'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não existe esse nome ! Você levou catfish :(";
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
        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT * FROM User";
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

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sb.toString();
    }

    public String listContacts(int user_id){

        StringBuilder sb = new StringBuilder();
    try {

        Statement statement = db.createStatement();
        String sqlQuery = """
                          SELECT u1.username,u1.name,u1.status,u1.last_seen
                          FROM User u1, Contact c1
                          WHERE c1.to_user_id = u1.user_id
                          AND c1.from_user_id = %d
                          UNION
                          SELECT u2.username,u2.name,u2.status,u2.last_seen
                          FROM User u2, Contact c2
                          WHERE c2.from_user_id = u2.user_id
                          AND c2.to_user_id = %d
                          AND c2.request_state LIKE '%%approved%%';     
                          """.formatted(user_id, user_id);
        ResultSet resultSet = statement.executeQuery(sqlQuery);
        if(!resultSet.next()){
            return "Não tem contactos! Faça amigos novos!";
        }else{
            do{
                sb.append("Utilizador: Nome:").append(resultSet.getString("name"))
                        .append("\tUsername: ").append(resultSet.getString("username"))
                        .append("\tStatus: ").append(resultSet.getString("status"))
                        .append("\tLast seen: ").append(resultSet.getString("last_seen")).append("\n");
            }while(resultSet.next());
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
                        AND request_state LIKE '%%approved%%'
                        """.formatted(getUserID(username_member), group_id);
        try (Statement statement = db.createStatement();) {
            statement.executeQuery(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isContact(String from_username, String toUsername){
        String data = listContacts((int)getUserID(from_username));
        return data.contains(toUsername);
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

    public String getGroupnameById(int group_id){
        try (Statement statement = db.createStatement()){
            String sqlQuery = "SELECT group_name FROM Group WHERE group_id = '" + group_id + "'";
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
        String sqlQuery = """
                INSERT INTO Data(read_state, sent_date, from_user_id, to_user_id, data_type, content)
                VALUES('waiting', NOW(), %d, %d, '%s', '%s');
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
        String sqlQuery = """
                INSERT INTO Data(read_state, sent_date, from_user_id, to_group_id, data_type, content)
                VALUES('waiting', NOW(), %d, %d, '%s', '%s');
                """.formatted(data.getUserData().getUserID(), data.getToGroupId(), data.getDataType(), data.getContent());
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sqlQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String pendConact(String username){
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
            System.err.println("SQLExeption pendContact");
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

    public void acceptOrRejectUpdate(String fromUsername, String toUsername,String acceptReject){
        try {
            if (acceptReject.equalsIgnoreCase("accept")) {
                PreparedStatement prepStatement = null;
                prepStatement = db.prepareStatement("UPDATE Contact SET request_state = ? WHERE to_user_id =? AND from_user_id =?");
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

    /*public boolean deleteUser(String username){
        try {
            Statement  statement = db.createStatement();
            String sqlQuery = "DELETE FROM User WHERE username='" + username + "'";
            statement.executeUpdate(sqlQuery);
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteUser");
            e.printStackTrace();
            return false;
        }
        return true;
    }*/

    public String deleteContact(String from_username, String toUsername) {
        if (isContact(from_username, toUsername)) {
            try {
                Statement statement = db.createStatement();
                String sqlQuery = """
                        DELETE FROM Contact
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND from_user_id = %d OR to_user_id = %d
                        AND request_state LIKE '%%approved%%'
                        """.formatted(getUserID(from_username), getUserID(from_username), getUserID(toUsername), getUserID(toUsername));
                if (statement.executeUpdate(sqlQuery) == 2) {

                    statement.close();
                    return "Não foi possivel eliminar o contacto " + toUsername;
                }
            } catch (SQLException e) {
                System.err.println("SQLExeption deleteContact");
                e.printStackTrace();
                return "SQLExeption deleteContact";
            }
            deleteMsgsAndFiles(getUserID(from_username), getUserID(toUsername));
            return "Contacto Eliminado com sucesso! ";
        }
        return "Esse contacto não existe! Tem a certeza que tem amigos ? ";
    }

    public void deleteMsgsAndFiles(long from_UserID, long to_UserID){
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                        DELETE FROM Data
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND from_user_id = %d OR to_user_id = %d
                        AND data_type LIKE '%%Message%%' 
                        OR data_type LIKE '%%File%%' 
                        """.formatted(from_UserID, from_UserID, to_UserID, to_UserID);
            statement.executeUpdate(sqlQuery);
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteContact");
            e.printStackTrace();
        }
    }

    public StringBuilder executeQuery(String sqlQuery){
        StringBuilder sb = new StringBuilder();

        try {
            Statement statement = db.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while(resultSet.next())
            {
                sb.append("id:" + resultSet.getInt("user_id"));
                sb.append("\tname:" + resultSet.getString("name"));
                sb.append("\tusername:" + resultSet.getString("username"));
                sb.append("\tlast_seen:" + resultSet.getString("last_seen"));
                sb.append("\tstatus:" + resultSet.getString("status") + "\n");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption listUsers");
            e.printStackTrace();
            return null;
        }
        return sb;
    }
}
