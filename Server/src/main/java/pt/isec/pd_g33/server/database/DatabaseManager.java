package pt.isec.pd_g33.server.database;

import java.sql.Connection;

public class DatabaseManager {

    private Connection db;

    public DatabaseManager(Connection db) {
        this.db = db;
    }



}
