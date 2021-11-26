package pt.isec.pd_g33.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private final String bdmsLocation;
    private final String username = "root";
    private final String password = "1234";

    private Connection db;

    public DatabaseConnection(String bdmsLocation) {
        this.bdmsLocation = "jdbc:mysql://" + bdmsLocation + "/MessengerDB?autoReconnect=true";
        System.out.println(this.bdmsLocation);

        try {
            db = DriverManager.getConnection(this.bdmsLocation, username, password);
        } catch (SQLException e) {
            //todo: tratar de quando a conexão ao servidor corre mal
            System.err.println("SQLExeption: Ocorreu um erro na conexão ao servidor");
            e.printStackTrace();
        }
    }

    public void close() {
        if(db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection(){
        try {
            return DriverManager.getConnection(this.bdmsLocation, username, password);
        } catch (SQLException e) {
            System.err.println();
            e.printStackTrace();
        }
        return null;
    }

    public Connection getDb() {
        return db;
    }

}
