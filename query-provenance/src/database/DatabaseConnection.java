package database;

import helper.InputHandler;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author mkjodhani
 * @project Query Provenance
 * @since 19/10/23
 * @version 1
 */
public class DatabaseConnection {
    private static Connection connection = null;
    public static Connection getConnection() {
        if (connection == null) {
            String database, username, password;
            database = InputHandler.getString("Provide Database name");
            username = InputHandler.getString("Provide Username");
            password = InputHandler.getString("Provide Password");
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/" + database.trim(),
                                username.trim(), password.trim());
                return connection;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
                return null;
            }
        } else {
            return connection;
        }
    }
}
