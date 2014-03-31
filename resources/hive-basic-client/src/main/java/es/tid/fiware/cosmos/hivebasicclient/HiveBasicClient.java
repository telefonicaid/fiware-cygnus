package es.tid.fiware.cosmos.hivebasicclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Francisco Romero Bueno frb@tid.es
 * 
 * Basic remote client for Hive mimicing the native Hive CLI behaviour.
 * 
 * Can be used as the base for more complex clients, interactive or not interactive.
 */
public final class HiveBasicClient {
    // JDBC driver required for Hive connections
    private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
    private static Connection con;
    
    /**
     * Constructor.
     */
    private HiveBasicClient() {
    } // HiveBasicClient

    /**
     * 
     * @param hiveServer
     * @param hivePort
     * @param hadoopUser
     * @param hadoopPassword
     * @return
     */
    private static Connection getConnection(String hiveServer, String hivePort, String hadoopUser,
            String hadoopPassword) {
        try {
            // dynamically load the Hive JDBC driver
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        } // try catch
      
        try {
            System.out.println("Connecting to jdbc:hive://" + hiveServer + ":" + hivePort
                    + "/default?user=" + hadoopUser + "&password=XXXXXXXXXX");
            // return a connection based on the Hive JDBC driver
            return DriverManager.getConnection("jdbc:hive://" + hiveServer + ":" + hivePort
                    + "/default?user=" + hadoopUser + "&password=" + hadoopPassword);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        } // try catch
    } // getConnection

    /**
     * 
     * @param query
     */
    private static void doQuery(String query) {
        try {
            // from here on, everything is SQL!
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery(query);

            // iterate on the result
            while (res.next()) {
                String s = "";

                for (int i = 1; i < res.getMetaData().getColumnCount(); i++) {
                    s += res.getString(i) + ",";
                } // for
              
                s += res.getString(res.getMetaData().getColumnCount());
                System.out.println(s);
            } // while

            // close everything
            res.close();
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.exit(0);
        } // try catch
    } // doQuery

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        // get the arguments
        String hiveServer = args[0];
        String hivePort = args[1];
        String cosmosUser = args[2];
        String cosmosPassword = args[3];
        
        // get a connection to the Hive server running on the specified IP address, listening on 10000/TCP port
        // authenticate using my credentials
        con = getConnection(hiveServer, hivePort, cosmosUser, cosmosPassword);
        
        if (con == null) {
            System.out.println("Could not connect to the Hive server!");
            System.exit(-1);
        } // if
        
        while (true) {
            // prompt the user for a set of HiveQL sentence (';' separated)
            System.out.print("remotehive> ");
            
            // open the standard input
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            // read the HiveQL sentences from the standard input
            String hiveqlSentences = null;
            
            try {
                hiveqlSentences = br.readLine();
            } catch (IOException e) {
                System.out.println("IO error trying to read a HiveQL query: " + e.getMessage());
                System.exit(1);
            } // try catch
            
            if (hiveqlSentences != null) {
                // get all the queries within the input HiveQL sentences
                String[] queries = hiveqlSentences.split(";");
                
                // for each query, execute it
                for (int i = 0; i < queries.length; i++) {
                    doQuery(queries[i]);
                } // for
            } // if
        } // while
    } // main
    
} //HiveClientTest
