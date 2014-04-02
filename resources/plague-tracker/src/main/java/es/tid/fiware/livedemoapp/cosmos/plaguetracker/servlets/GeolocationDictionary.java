package es.tid.fiware.livedemoapp.cosmos.plaguetracker.servlets;



import java.util.ArrayList;

/**
 *
 * @author Francisco Romero Bueno frb@tid.es
 */
public class GeolocationDictionary {
    
    /**
     * 
     */
    public class Coordinates {
        
        private String lat;
        private String lng;
        
        public String getLat() {
            return lat;
        } // getLat
        
        public String getLng() {
            return lng;
        } // gertLng
        
    } // Coordiantes
    
    /**
     * 
     */
    public class Geolocation {
        
        private String neighbourhood;
        private Coordinates center;
        private ArrayList<Coordinates> area;
        
        public Geolocation() {
            center = new Coordinates();
            area = new ArrayList<Coordinates>();
        } // Geolocation
        
    } // Geolocation
    
    private ArrayList<Geolocation> geolocations;
    
    /**
     * 
     */
    public GeolocationDictionary() {
        geolocations = new ArrayList<Geolocation>();
    } // GeolocationDictionary
    
    /**
     * 
     * @param neighbourhood
     * @return
     */
    public Coordinates getCenter(String neighbourhood) {
        for (int i = 0; i < geolocations.size(); i++) {
            Geolocation geolocation = geolocations.get(i);
            
            if (geolocation.neighbourhood.equals(neighbourhood)) {
                return geolocation.center;
            } // if
        } // for
        
        return null;
    } // getCenter
    
    /**
     * 
     * @param neighbourhood
     * @return
     */
    public ArrayList<Coordinates> getArea(String neighbourhood) {
        for (int i = 0; i < geolocations.size(); i++) {
            Geolocation geolocation = geolocations.get(i);
            
            if (geolocation.neighbourhood.equals(neighbourhood)) {
                return geolocation.area;
            } // if
        } // for
        
        return null;
    } // getCenter
    
} // GeolocationDictionary
