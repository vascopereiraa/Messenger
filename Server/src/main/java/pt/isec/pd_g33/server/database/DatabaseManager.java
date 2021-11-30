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
        try {
            db = DriverManager.getConnection(this.bdmsLocation, username, password);
        } catch (SQLException e) {
            //todo: tratar de quando a conexão ao servidor corre mal
            System.err.println("SQLExeption: Ocorreu um erro na conexão a base de dados");
            e.printStackTrace();
        }
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

    public Data searchUserByName(String user) {
        String sqlQuery = "SELECT user_id,name, username, last_seen, " +
                "status FROM User WHERE name LIKE '%" + user + "%'";
        return new Data(executeQuery(sqlQuery).toString());
    }

    public Data listUsers(){
        String sqlQuery = "SELECT * FROM User";
        return new Data(executeQuery(sqlQuery).toString());
    }

    public boolean deleteUser(String username){
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
                sb.append("\tsatus:" + resultSet.getString("status") + "\n");
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
