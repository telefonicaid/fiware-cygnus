package es.tid.fiware.livedemoapp.cosmos.plaguetracker.servlets;



import com.google.gson.Gson;
import es.tid.fiware.livedemoapp.cosmos.plaguetracker.hive.HiveConnection;
import es.tid.fiware.livedemoapp.cosmos.plaguetracker.servlets.GeolocationDictionary.Coordinates;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Francisco Romero Bueno frb@tid.es
 */
@WebServlet(name = "CommandsMgr", urlPatterns = { "/CommandsMgr" })

public class CommandsMgr extends HttpServlet {
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
        // get a writer from the response object in order to send back to the client plain text data
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
            
        // parameters that can be sent in the request
        String cmd = null; // command to be executed by this servlet
        String type = null; // type of plague
        
        // get all the request parameters
        Enumeration<String> keys = request.getParameterNames();
        
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            if (key.equalsIgnoreCase("cmd")) {
                cmd = request.getParameter(key);
            } else if (key.equalsIgnoreCase("type")) {
                type = request.getParameter(key);
            } // if else if
        } // while
        
        // process the command
        String msg = null;
        
        if (cmd == null) {
            msg = "";
        } else if (cmd.equalsIgnoreCase("getCurrentFocuses")) {
            msg = getCurrentFocusesPerNeighbourhood(type);
        } else if (cmd.equalsIgnoreCase("getInfectionLevels")) {
            msg = getInfectionLevelsPerNeighbourhood(type);
        } else if (cmd.equalsIgnoreCase("getCorrelations")) {
            msg = getCorrelationsPerMonth(type);
        } // if else if else if
        
        try {
            out.println(msg);
        } finally {
            out.close();
        } // try finally
    } // doGet

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
        // get a writer from the response object in order to send back to the client plain text data
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // parameters that can be sent in the request
        
        String cmd = null; // command to be executed by this servlet
        
        // get all the request parameters
        Enumeration<String> keys = request.getParameterNames();
        
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            if (key.equalsIgnoreCase("cmd")) {
                cmd = request.getParameter(key);
            } // if
        } // while
        
        // process the command
        String msg = null;
        
        if (cmd == null) {
            msg = "";
        } // if
        
        try {
            out.println(msg);
        } finally {
            out.close();
        } // try finally
    } // doPost

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    } // getServletInfo
    
    /**
     * 
     * @param type Plague type
     * @return A json string containing the hits per neighbourhood
     */
    private String getInfectionLevelsPerNeighbourhood(String type) {
        GeolocationDictionary geolocations = loadCoordinates();
        
        // query the Cosmos cluster
        Connection con = HiveConnection.getConnection("130.206.80.46", "10000");
        
        try {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery(
                "select neighbourhood,count(*) from malaga_plagues where type='" + type + "' group by neighbourhood");
            String s = "{\"infection_levels\": [";
            boolean empty = true;
        
            // iterate on the result in order to create a json string
            while (res.next()) {
                String neighbourhood = res.getString(1);
                String count = res.getString(2);
                ArrayList<Coordinates> area = geolocations.getArea(neighbourhood);
                
                if (area == null) {
                    continue;
                } // if
                
                empty = false;
                s += "{\"count\": \"" + count + "\", \"area\": [";
                
                for (int i = 0; i < area.size(); i++) {
                    Coordinates coords = area.get(i);
                    
                    s += "{\"lat\": \"" + coords.getLat() + "\","
                        + "\"lng\": \"" + coords.getLng() + "\"},";
                } // for
                
                s = s.substring(0, s.length() - 1) + "]},";
            } // while
            
            // close everything
            res.close();
            stmt.close();
            con.close();
            
            // return the json string
            if (empty) {
                return s + "]}";
            } else {
                return s.substring(0, s.length() - 1) + "]}";
            } // if else
        } catch (java.sql.SQLException e) {
            Logger.getLogger(CommandsMgr.class.getName()).log(Level.SEVERE, null, e);
            return "{\"infection_levels\": []}";
        } // try catch
    } // getInfectionLevelsPerNeighbourhood
    
    /**
     * 
     * @return
     */
    private String getCurrentFocusesPerNeighbourhood(String type) {
        GeolocationDictionary geolocations = loadCoordinates();
        
        // query the Cosmos cluster
        Connection con = HiveConnection.getConnection("130.206.80.46", "10000");
        
        try {
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select neighbourhood from malaga_plagues where type='" + type + "'");
            String s = "{\"focuses\": [";
            boolean empty = true;
        
            // iterate on the result in order to create a json string
            while (res.next()) {
                String neighbourhood = res.getString(1);
                ArrayList<Coordinates> area = geolocations.getArea(neighbourhood);
                
                if (area == null) {
                    continue;
                } // if
                
                empty = false;
                s += "{\"area\": [";
                
                for (int i = 0; i < area.size(); i++) {
                    Coordinates coords = area.get(i);
                    
                    s += "{\"lat\": \"" + coords.getLat() + "\","
                        + "\"lng\": \"" + coords.getLng() + "\"},";
                } // for
                
                s = s.substring(0, s.length() - 1) + "]},";
            } // while
            
            // close everything
            res.close();
            stmt.close();
            con.close();
            
            // return the json string
            if (empty) {
                return s + "]}";
            } else {
                return s.substring(0, s.length() - 1) + "]}";
            } // if else
        } catch (java.sql.SQLException e) {
            Logger.getLogger(CommandsMgr.class.getName()).log(Level.SEVERE, null, e);
            return "{\"focuses\": []}";
        } // try catch
        
    } // getCurrentFocusesPerNeighbourhood
    
    /**
     * 
     * @param type
     * @return
     */
    private String getCorrelationsPerMonth(String type) {
        // query the Cosmos cluster
        Connection con = HiveConnection.getConnection("130.206.80.46", "10000");
        
        try {
            Statement stmt = con.createStatement();
/*            
            ResultSet res = stmt.executeQuery(
                    "select c1,c4,c5,c6,c2 from (select month(date_) as c1,count(*) as c2 from malaga_plagues "
                    + "where type='" + type + "' group by month(date_)) table1 join (select month(date_) as c3,"
                    + "sum(avg_temp) as c4,sum(rainfall) as c5,sum(humidxity) as c6 from malaga_weather "
                    + "where date_ like '%2013%' group by month(date_)) table2 on table1.c1 = table2.c3");
*/
            ResultSet res = stmt.executeQuery(
                    "select c1,c4,c5,c6,c2 from (select month(date_) as c1,count(*) as c2 from malaga_plagues "
                    + "where type='" + type + "' group by month(date_)) table1 join (select month as c3,"
                    + "sum(tave) as c4,sum(p24) as c5,sum(r_max_vel) as c6 from malaga_meteo "
                    + "where year like '%2013%' group by month) table2 on table1.c1 = table2.c3");
            
            String s = "{\"correlations\": [";
            boolean empty = true;
        
            // iterate on the result in order to create a json string
            while (res.next()) {
                empty = false;
                String month = res.getString(1);
                String avgTemp = res.getString(2);
                String avgRainfall = res.getString(3);
                String avgHumidity = res.getString(4);
                String numIncidents = res.getString(5);
                
                s += "{\"month\": \"" + month + "\", \"avg_temp\": \"" + avgTemp + "\", \"avg_rainfall\": \""
                        + avgRainfall + "\", \"avg_humidity\": \"" + avgHumidity + "\", \"num_incidents\": \""
                        + numIncidents + "\"},";
            } // while
        
            // close everything
            res.close();
            stmt.close();
            con.close();
            
            // return the json string
            if (empty) {
                return s + "]}";
            } else {
                return s.substring(0, s.length() - 1) + "]}";
            } // if else
        } catch (SQLException ex) {
            Logger.getLogger(CommandsMgr.class.getName()).log(Level.SEVERE, null, ex);
            return "{\"correlations\": []}";
        } // try catch
    } // getCorrelationsPerMonth
    
    private GeolocationDictionary loadCoordinates() {
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(
                    this.getServletContext().getRealPath("config/geolocations.json")));
        } catch (Exception e) {
            System.out.append(e.getMessage());
            return null;
        } // try catch
        
        String jsonStr = "";
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                jsonStr += line;
            } // while
        } catch (Exception e) {
            System.out.append(e.getMessage());
            return null;
        } // try catch
        
        Gson gson = new Gson();
        GeolocationDictionary geolocations = gson.fromJson(jsonStr, GeolocationDictionary.class);
        
        try {
            reader.close();
        } catch (IOException e) {
            System.out.append(e.getMessage());
            return null;
        } // try catch
        
        return geolocations;
    } // getCoordinates
    
} // CommandsMgr
