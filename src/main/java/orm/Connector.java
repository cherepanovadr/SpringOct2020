package orm;

import java.sql.Connection;
import java.util.Properties;

public class Connector {
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static void createConnection(String dbName, String user, String password){
        Properties props = new Properties();
        props.setProperty("user", user);
        
    }
}
