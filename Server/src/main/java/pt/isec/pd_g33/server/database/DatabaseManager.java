package pt.isec.pd_g33.server.database;

import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.UserData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseManager {
    private DatabaseConnection dbConnection;
    private Connection db;

    public DatabaseManager(DatabaseConnection db) {
        this.dbConnection = db;
        this.db = dbConnection.getConnection();
    }

    public UserData insertUser(String name, String username, String password){
        UserData userData;
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "INSERT INTO User(name, username, password, last_seen, status)" +
                    "VALUES('" + name + "','" + username + "','" + password + "','"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "','Online')";
            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);

            // Para obter o id do utilizador, para ser possivel mais tarde atualizar dados do mesmo
            String sqlQuery1 = "SELECT user_id FROM User WHERE username = '" + username + "'";
            ResultSet resultSet =  statement.executeQuery(sqlQuery1);

            if(resultSet.next()){
                userData = new UserData(resultSet.getInt("user_id"),
                                           username,password,name);
            }else
                return null;

        } catch(SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return null;
        }
        return userData;
    }

    //todo: check if it works
    public boolean updateUser(String name, String newUsername, String password, int userID){
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "UPDATE User set(name, username, password)" +
                    "VALUES('" + name + "','" + newUsername + "','" + password +
                    ")' WHERE user_id =" + userID + "";

            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);
        } catch(SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public UserData checkUserLogin(String username, String password){
        UserData userData;
        try {
            Statement statement = db.createStatement();
            String sqlQuery = "SELECT user_id,username,password,name FROM User WHERE username = '" + username + "' AND password ='" + password + "'";
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

    public Data searchUserByName(String user){
        String sqlQuery = "SELECT name, username, status FROM User WHERE name='" + user + "'";
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

    //todo: fix this function
    public StringBuilder executeQuery(String sqlQuery){
        StringBuilder sb = new StringBuilder();

        try {
            Statement statement = db.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while(resultSet.next())
            {
                int user_id = resultSet.getInt("user_id");
                sb.append("name:" + resultSet.getString("name"));
                sb.append("username: " + resultSet.getString("username"));
                sb.append("last_seen: " + resultSet.getString("last_seen"));
                sb.append("satus: " + resultSet.getString("status"));
            }
            resultSet.close();
            statement.close();

            return sb;

        } catch (SQLException e) {
            System.err.println("SQLExeption listUsers");
            e.printStackTrace();
        }

        return null;
    }


}
