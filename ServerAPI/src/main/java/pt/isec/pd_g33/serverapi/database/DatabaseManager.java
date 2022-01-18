package pt.isec.pd_g33.serverapi.database;

import org.apache.tomcat.jni.Local;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {

    private final String bdmsLocation = "jdbc:mysql://localhost:3306/MessengerDB";
    private final String username = "root";
    private final String password = "1234";
    private static Connection db;

    public DatabaseManager(){
        try {
            db = DriverManager.getConnection(this.bdmsLocation, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkToken(String token){
        if(token == null)
            return false;

        try(Statement statement = db.createStatement()) {
            String sqlQuery = "SELECT token_creation FROM User WHERE BINARY token = '" + token + "';";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(resultSet.next()){
               Timestamp d = resultSet.getTimestamp("token_creation");
               LocalDateTime tokenTime = d.toLocalDateTime().plusMinutes(2);
               LocalDateTime agora = LocalDateTime.now();
               if(agora.isBefore(tokenTime))
                   return true;
               else
                   return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
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

    public String editUser(String token, String newName) {
        try {
            PreparedStatement statement = db.prepareStatement("UPDATE User SET name = ? WHERE token =?");
            statement.setString(1, newName);
            statement.setString(2, token);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLException: Erro no update do user.");
            e.printStackTrace();
            return "Ocorreu um erro a atualizar o utilizador";
        }
        return "Utilizador atualizado com sucesso";
    }

    private int getUserIDByName(String name) {
        try (Statement statement = db.createStatement()) {
            String sqlQuery1 = "SELECT user_id FROM User WHERE BINARY username = '" + name + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery1);
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else
                return -1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public String removeContact(String token, String contactName) {
        String fromUsername = getUsernameById(getUserID(token));
        if (isContact(token, contactName)) {
            int userId = getUserID(token);
            int contactId = getUserIDByName(contactName);
            try {
                Statement statement = db.createStatement();
                String sqlQuery = """ 
                        DELETE FROM Contact
                        WHERE to_user_id = %d OR from_user_id = %d
                        AND from_user_id = %d OR to_user_id = %d
                        AND request_state LIKE '%%approved%%'
                        """.formatted(userId, userId, contactId, contactId);
                if (statement.executeUpdate(sqlQuery) == 2) {
                    statement.close();
                    return "Não foi possivel eliminar o contacto " + contactName;
                }
                statement.close();
            } catch (SQLException e) {
                System.err.println("SQLExeption deleteContact");
                e.printStackTrace();
                return "SQLExeption deleteContact";
            }
            deleteMsgsAndFiles(userId, contactId);
            return "Contacto Eliminado com sucesso! ";
        }
        return "Esse contacto não existe na sua lista de contactos!\n";
    }

    public String getContactMsg(String token, int contactId) {
        String username = getUsernameById(getUserID(token));
        String contactName = getUsernameById(contactId);
        if(!isContact(token,contactName))
            return "Indique por favor um contacto seu válido.";

        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT from_user_id, content, read_state
                    FROM Data
                    WHERE from_user_id = %d
                    AND to_user_id = %d
                    AND data_type = '%s'
                    """.formatted(contactId, getUserID(token), "message");
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem mensagens com o contacto " +contactName;
            } else {
                do {
                    sb.append("\nO [").append(contactName)
                            .append("] enviou uma mensagem para o contacto [").append(username).append(']')
                            .append("\tRead State: ").append(resultSet.getString("read_state"))
                            .append("\nMensagem: ").append(resultSet.getString("content")).append("\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLExeption: getContactMsg";
        }
        return sb.toString();
    }

    public String getGroupMsg(String token, int groupId){
        /*String fromUsername = getUsernameById(getUserID(token));*/
        if(!belongsToGroup(token,groupId))
            return "Indique por favor um grupo a que pertença.";

        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT from_user_id, content, read_state
                    FROM Data
                    WHERE to_group_id = %d 
                    AND data_type = '%s'
                    """.formatted(groupId,"Message");
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não existem mensagens no grupo " + getGroupNameById(groupId);
            } else {
                do {
                    sb.append("\nO [").append(getUsernameById(resultSet.getInt("from_user_id")))
                            .append("] enviou uma mensagem para o grupo [").append(getGroupNameById(groupId)).append(']')
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


    public String listContacts(String token) {
        int userId = getUserID(token);
        StringBuilder sb = new StringBuilder();
        try {

            Statement statement = db.createStatement();
            String sqlQuery = """
                    SELECT u1.user_id, u1.username,u1.name,u1.status,u1.last_seen
                    FROM User u1, Contact c1
                    WHERE c1.to_user_id = u1.user_id
                    AND c1.from_user_id = %d
                    AND c1.request_state LIKE '%%approved%%'
                    UNION
                    SELECT u2.user_id, u2.username,u2.name,u2.status,u2.last_seen
                    FROM User u2, Contact c2
                    WHERE c2.from_user_id = u2.user_id
                    AND c2.to_user_id = %d
                    AND c2.request_state LIKE '%%approved%%';
                    """.formatted(userId, userId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if (!resultSet.next()) {
                return "Não tem contactos!";
            } else {
                do {
                    sb.append("Utilizador: ").append("\n")
                            .append("\tUser ID:").append(resultSet.getString("user_id")).append("\n")
                            .append("\tNome:").append(resultSet.getString("name")).append("\n")
                            .append("\tUsername: ").append(resultSet.getString("username")).append("\n")
                            .append("\tStatus: ").append(resultSet.getString("status")).append("\n")
                            .append("\tLast seen: ").append(resultSet.getString("last_seen")).append("\n\n");
                } while (resultSet.next());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQLExeption: listContacts";
        }
        return sb.toString();

    }

    public String listGroups(String token) {
        int userId = getUserID(token);
        StringBuilder sb = new StringBuilder();
        try {
            Statement statement = db.createStatement();
        String sqlQuery="""
                        SELECT p.group_id, g.group_name
                        FROM `Group` g, Participate p
                        WHERE p.user_id = %d
                        AND p.group_id = g.group_id
                        """.formatted(userId);
        ResultSet resultSet = statement.executeQuery(sqlQuery);
        if (!resultSet.next()) {
            return "Não pertence a nenhum grupo.";
        } else {
            do {
                sb.append("GrupoId: ").append(resultSet.getInt("group_id")).append("\t")
                        .append("Name: ").append((resultSet.getString("group_name"))).append("\n");
            } while (resultSet.next());
        }
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
        return "SQLExeption: listGroups";
    }
        return sb.toString();
    }

    // Metodos auxiliares
    public int getUserID(String token) {
        try (Statement statement = db.createStatement()) {
            String sqlQuery1 = "SELECT user_id FROM User WHERE BINARY token = '" + token + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery1);
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else
                return -1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public boolean isContact(String token, String toUsername){
        String data = listContacts(token);
        return data.contains(toUsername);
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

    public boolean belongsToGroup(String token, int groupId){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                    SELECT *
                    FROM Participate
                    WHERE user_id = %d
                    AND group_id = %d
                    AND membership_state LIKE '%%approved%%';
                    """.formatted(getUserID(token), groupId);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public boolean loginUser(String username, String password, String token) {
        int user_id = -1;
        try (Statement statement = db.createStatement()) {
            String sqlQuery = """
                SELECT user_id
                FROM User
                WHERE BINARY username = '%s'
                AND BINARY password = '%s';
                """.formatted(username, password);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(resultSet.next()) {
                user_id = resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            return false;
        }

        if(user_id != -1) {
            try (Statement statement = db.createStatement()) {
                String sqlQuery = """
                UPDATE User
                SET token = '%s', token_creation = NOW()
                WHERE BINARY user_id = '%d';
                """.formatted(token, user_id);
                statement.executeUpdate(sqlQuery);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        return false;
    }
}
