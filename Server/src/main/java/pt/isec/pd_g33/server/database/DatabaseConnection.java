package pt.isec.pd_g33.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private final String bdmsLocation;
    private final String username = "root";
    private final String password = "1234";

    private Connection db;

    public DatabaseConnection(String bdmsLocation) {
        System.out.println(bdmsLocation);
        this.bdmsLocation = "jdbc:mysql://" + bdmsLocation;

        try {
            db = DriverManager.getConnection(bdmsLocation, username, password);
        } catch (SQLException e) {
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

    public Connection getDb() {
        return db;
    }

}
