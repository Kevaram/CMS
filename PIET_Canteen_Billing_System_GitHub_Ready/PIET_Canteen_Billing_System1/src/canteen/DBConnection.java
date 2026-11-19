package canteen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = System.getenv().getOrDefault("PIET_DB_URL", "jdbc:mysql://localhost:3306/piet_canteen_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true");
    private static final String USER = System.getenv().getOrDefault("PIET_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("PIET_DB_PASSWORD", "root");
    private DBConnection() {}
    public static Connection getConnection() throws SQLException { return DriverManager.getConnection(URL, USER, PASSWORD); }
    public static boolean isConnected() { try (Connection c=getConnection()) { return c.isValid(3); } catch (SQLException e) { return false; } }
    public static void main(String[] args) {
        try (Connection con=getConnection()) { System.out.println("MySQL Database Connected Successfully: " + con.getCatalog()); }
        catch (SQLException e) { System.err.println("Database connection failed. Check MySQL service, database name, PIET_DB_USER and PIET_DB_PASSWORD."); System.err.println(e.getMessage()); }
    }
}
