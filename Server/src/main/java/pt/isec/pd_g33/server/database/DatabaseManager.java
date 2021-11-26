package pt.isec.pd_g33.server.database;

import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.UserData;

import java.sql.*;
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

    public UserData insertUser(String name, String username, String password) {
        UserData userData;
        try (Statement statement = db.createStatement()) {
            String sqlQuery = "INSERT INTO User(name, username, password, last_seen, status)" +
                    "VALUES('" + name + "','" + username + "','" + password + "','"
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "','Online')";
            System.out.println(sqlQuery);
            statement.executeUpdate(sqlQuery);

            // Para obter o id do utilizador, para ser possivel mais tarde atualizar dados do mesmo
            String sqlQuery1 = "SELECT user_id FROM User WHERE username = '" + username + "'";
            ResultSet resultSet = statement.executeQuery(sqlQuery1);

            if (resultSet.next()) {
                userData = new UserData(resultSet.getInt("user_id"),
                        username, password, name);
            } else
                return null;

        } catch (SQLException e) {
            System.err.println("SQLException: Cliente já existe com esse nome");
            e.printStackTrace();
            return null;
        }
        return userData;
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
                sb.append("\tsatus:" + resultSet.getString("status"));
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
