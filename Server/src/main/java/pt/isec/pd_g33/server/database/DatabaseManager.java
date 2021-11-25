package pt.isec.pd_g33.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseManager {

    private DatabaseConnection dbConnection;
    private Connection db;

    public DatabaseManager(DatabaseConnection db) {
        this.dbConnection = db;
        this.db = dbConnection.getConnection();
    }

    public boolean insertUser(String name, String username, String password){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "INSERT INTO User(name, username, password, last_seen, status)" +
                    "VALUES('" + name + "','" + username + "','" + password + "','"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "','Online')";
            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);
        } catch(SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //todo: check if it works
    public boolean updateUser(String name, String username, String password){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "UPDATE User set(name, username, password, last_seen, status)" +
                    "VALUES('" + name + "','" + username + "','" + password + "','"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "','Online')";
            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);
        } catch(SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean checkUserLogin(String username, String password){
        boolean userExists;
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT username FROM User WHERE username = '" + username + "' AND password ='" + password + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            if(resultSet.next()){
                userExists = true;
            }else
                userExists = false;

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return userExists;
    }

    public void listUsers(){
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT user_id, name, username, password, status, last_seen FROM User";

            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while(resultSet.next())
            {
                int user_id = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String last_seen = resultSet.getString("last_seen");
                String status = resultSet.getString("status");
                System.out.println("[" + user_id + "] name: " + name + " | username: "
                        + username + " | password: " + password + " | last_seen: " + last_seen + " | status: " + status);
            }
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("SQLExeption listUsers");
            e.printStackTrace();
        }
    }

    public boolean deleteUser(String username){
        try {
            Statement  statement = db.createStatement();
            String sqlQuery = "DELETE FROM users WHERE username='" + username + "'";
            statement.executeUpdate(sqlQuery);
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQLExeption deleteUser");
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
