package database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author mkjodhani
 * @since 19/10/23
 * @version 1
 */

/**
 * The DatabaseConnection class provides a simple mechanism for managing a database connection using JDBC.
 * It uses the PostgreSQL JDBC driver for establishing a connection.
 * <p>
 * The class follows the Singleton pattern, ensuring that only one instance of the connection is maintained.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * // To establish a connection:
 * DatabaseConnection.connect("jdbc:postgresql://localhost:5432/mydatabase", "user", "password");
 *
 * // To retrieve the connection:
 * Connection connection = DatabaseConnection.getConnection();
 * }
 * </pre>
 *
 * <p><b>Note:</b> It is important to call {@code connect()} method before attempting to retrieve the connection.</p>
 *
 */
public class DatabaseConnection {

    private static Connection connection = null;

    /**
     * Retrieves the existing database connection.
     *
     * @return The database connection, or null if not connected.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Establishes a new database connection using the provided URL, username, and password.
     *
     * @param url      The URL of the database.
     * @param username The username for connecting to the database.
     * @param password The password for connecting to the database.
     * @return The established database connection, or null if an error occurs.
     */
    public static Connection connect(String url, String username, String password){
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection(url.trim(),
                            username.trim(), password.trim());
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
