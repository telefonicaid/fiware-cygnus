package es.tid.fiware.livedemoapp.cosmos.plaguetracker.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Romero Bueno frb@tid.es
 */
public final class HiveConnection {
    private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
    
    /**
     * Constructor. It is private since this is an utility class.
     */
    private HiveConnection() {
    } // getConnection
    
    /**
     * 
     */
    public static Connection getConnection(String ip, String port) {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
            
            System.exit(1);
        } // try catch
        
        try {
            return DriverManager.getConnection("jdbc:hive://" + ip + ":" + port
                    + "/default?user=user&password=password");
        } catch (SQLException ex) {
            Logger.getLogger(HiveConnection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } // catch
    } // getConnection
    
} // getConnection
