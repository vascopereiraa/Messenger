package pt.isec.pd_g33.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private Connection db;

    public DatabaseManager(Connection db) {
        this.db = db;
    }

    public boolean insertUser(String name, String username, String password){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "INSERT INTO users VALUES('" + name + "','" + username + "','" + password +"')";
            statement.executeUpdate(sqlQuery);
            statement.close();

        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean checkUserLogin(String username, String password){
        boolean userExists;
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT username FROM users WHERE username =" + username +" AND password =" + password + ")";
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            if(resultSet.next()){
                userExists = true;
            }else{
                userExists = false;
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return userExists;
    }

    






}
