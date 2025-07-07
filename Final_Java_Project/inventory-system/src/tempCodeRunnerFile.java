
// public class DatabaseUtil {
//     private static final String CONFIG_FILE = "config.properties";
//     private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_simple";
    
//     public static Connection getConnection() throws SQLException, IOException {
//         Properties props = new Properties();
//         props.load(new FileInputStream(CONFIG_FILE));
//         return DriverManager.getConnection(DB_URL, props);
//     }