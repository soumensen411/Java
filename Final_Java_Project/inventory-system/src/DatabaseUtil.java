import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;

public class DatabaseUtil {
    public static void initializeDatabase(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(60) NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, quantity INT NOT NULL DEFAULT 0, price DOUBLE NOT NULL DEFAULT 0.0, category VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, contact VARCHAR(100) NOT NULL, product_id INT, FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_log (id INT AUTO_INCREMENT PRIMARY KEY, action VARCHAR(100) NOT NULL, timestamp VARCHAR(50) NOT NULL, product_id INT, change_amount INT DEFAULT 0)");
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (username, password) VALUES ('admin', '$2a$10$Xo8f/..N3p8iFSEy67j3L.8m8C0lZc1C8kX9mL0pQ1K2L3M4N5O6P')");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Init Error: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Close Error: " + e.getMessage());
        }
    }
}