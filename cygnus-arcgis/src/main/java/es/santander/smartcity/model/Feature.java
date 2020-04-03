package es.santander.smartcity.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.santander.smartcity.exceptions.ArcgisException;


/**
 * @author dmartinez
 *
 */
public class Feature {
	private static final String DATE_PATTERN = "MM/dd/yyyy hh:mm:ss";
	private static final String OBJECTID_FIELDNAME = "OBJECTID";
	
	protected static final Logger logger = Logger.getLogger(Feature.class);
	private Geometry geometry;
	private Map<String, Object> attributes;
	
	/**
	 * 
	 */
	public Feature(){
		attributes = new HashMap<String,Object>();
		geometry = null;
	}
	
	/**
	 * 
	 * @param geometry
	 */
	public Feature(Geometry geometry){
		this();
		this.geometry = geometry;
	}
	
	/**
	 * 
	 * @param geometry
	 * @param attributes
	 */
	public Feature (Geometry geometry, Map<String, Object> attributes){
		this.geometry = geometry;
		this.attributes = attributes;
	}

	/**
	 * 
	 * @param attName
	 * @param attValue
	 */
	public void addAttribute (String attName, Object attValue){
		attributes.put(attName, attValue);
	}

	/**
	 * @return the geometry
	 */
	public Geometry getGeometry() {
		return geometry;
	}

	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * @return the attributes
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
			
	/**
	 * 
	 * @param latitud
	 * @param longitud
	 * @return
	 */	
	static public Feature createPointFeature (double latitud, double longitud){
		return new Feature(new Point(longitud, latitud, SpatialReference.WGS84));
	}
	
	/**
	 * 
	 * @param latitud
	 * @param longitud
	 * @return
	 */
	static public Feature createPointFeature (String latitud, String longitud){
		double lat;
		double lon;
		try {
			lat = Double.parseDouble(latitud);
			lon = Double.parseDouble(longitud);
		} catch (Exception e) {
			lat = new Double(0);
			lon = new Double(0);
		}
		
		return createPointFeature (lat, lon);
	}
	
	/**
	 * 
	 * @param lnglat
	 * @return
	 */
//	static public Feature createPointEtity (LngLat lnglat){
//		return createPointFeature(lnglat.getLat(), lnglat.getLng());
//	}

	/**
     * 
     * @param latitud
     * @param longitud
     * @return
     */ 
//    static public Feature createPolylineFeature (List<Point> listPoint){
//        PointCollection points=new PointCollection(listPoint);
//        return new Feature(new Polyline(points, SpatialReferences.getWgs84()));
//    }
//	
    
    /**
     * 
     * @param latitud
     * @param longitud
     * @return
     */ 
//    static public Feature createPolygonFeature (List<Point> listPoint){
//        PointCollection points=new PointCollection(listPoint);
//        return new Feature(new Polygon(points, SpatialReferences.getWgs84()));
//    }
    
    
    /**
     * This method merges unexistent attributes from sourceFeature
     * @param sourceFeature
     */
    public void completeAttributesFrom (Feature sourceFeature){
    	Map<String,Object> sourceAttributes = sourceFeature.getAttributes();
    	Set<String> sourceKeyset = sourceAttributes.keySet();
    	for (String key : sourceKeyset) {
			if (!this.attributes.containsKey(key)){
				this.attributes.put(key, sourceAttributes.get(key));
			}
		}
    }
    
	/**
	 * 
	 */
	@Override
	public String toString() {
        return toJson().toString();
	    
	}
	
	private void addProperty(JsonObject jsonObj, String name, Object property){
		SimpleDateFormat simpleDateFormat = null;
		
		if (property != null){
			switch (property.getClass().getSimpleName()){
			case "Integer":
				jsonObj.addProperty(name, (Integer)property);break;
			case "Long":
				jsonObj.addProperty(name, (Long)property);break;
			case "Float":
				jsonObj.addProperty(name, (Float)property);break;
			case "Double":
				jsonObj.addProperty(name, (Double)property);break;
			case "String":
				jsonObj.addProperty(name, (String)property);break;
			case "Boolean":
				jsonObj.addProperty(name, (Boolean)property);break;	
			case "Date":
				simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
				jsonObj.addProperty(name, simpleDateFormat.format((Date)property));
				break;	
			case "GregorianCalendar":
				simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
				GregorianCalendar calendar = (GregorianCalendar) property;
				jsonObj.addProperty(name, simpleDateFormat.format(calendar.getTime()));
				break;
			}
		}else {
			jsonObj.add(name, null);
		}
	}
	
	/**
	 * Retorna el OBJECTID del GIS de la entidad
	 * @return OBJECTID value
	 * @throws ArcgisException
	 */
	public Integer getObjectId() throws ArcgisException{
		Integer objectId = -1;
		for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
			if (OBJECTID_FIELDNAME.equals(attribute.getKey())){
				objectId =  (Integer) attribute.getValue();
				break;
			}
		}
		
		if ("".equals(objectId)){
			throw new ArcgisException("Cant find " + GisAttributeType.OID + " in Feature Object." );
		}else{
			return objectId;
		}
	}
	
	/**
	 * Establece el OBJECTID del GIS de la entidad
	 * @param objectId
	 * @return
	 * @throws ArcgisException
	 */
	public void setObjectId(Integer objectId) throws ArcgisException{
		try{
			boolean found = false;
			
			for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
				if (OBJECTID_FIELDNAME.equals(attribute.getKey())){
					 found = true;
					 attribute.setValue(objectId);
					 break;
				}
			}	
			if (!found) attributes.put(OBJECTID_FIELDNAME, objectId);
		}catch (Exception e){
			throw new ArcgisException("Error setting OBJECTID for feature " + this.toString() + " - Error: " + e);
		}
	}
	
	public JsonObject toJson(){
		JsonObject resultJSON = new JsonObject();
		
		resultJSON.add("geometry", this.getGeometry().toJSON());
		
		JsonObject attributes = new JsonObject();
		for (Map.Entry<String, Object> attribute : this.attributes.entrySet()) {
			addProperty(attributes, attribute.getKey(), attribute.getValue());
		}
		resultJSON.add("attributes", attributes);
	
		
		return resultJSON;
	}
	
	public static Feature createInstanceFromJson (String json) throws ArcgisException{
        JsonParser parser = new JsonParser();
        JsonObject jsonObj = parser.parse(json).getAsJsonObject();
        return createInstanceFromJson(jsonObj);
	}
	
	public static Feature createInstanceFromJson (JsonObject json) throws ArcgisException{
		try{
			Geometry geometry;
			if (json.has("geometry")){
				JsonObject jsonGeometry = json.get("geometry").getAsJsonObject();
				geometry = Point.createInstanceFromJson(jsonGeometry); //TODO ¿distintos tipos de geometría?
			} else {
				geometry = new Point(0,0);
			}
			Map<String, Object> attributes = attToMap(json.get("attributes").getAsJsonObject());
			return  new Feature(geometry, attributes);
		}catch (Exception e){
			throw new ArcgisException("Can't cast Feature from Json, " 
										+ e.getClass().getSimpleName() 
										+ " - " + e.getMessage());
		}
		
	}
	
	protected static Map<String, Object> attToMap(JsonObject attributes) throws ArcgisException{
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if (attributes.isJsonObject()){
			for (Entry<String, JsonElement> attribute : attributes.entrySet()) {
				JsonElement attValue = attribute.getValue();
				if (attValue.isJsonPrimitive()){
					try{
						int intValue = attValue.getAsInt();
						if (attValue.getAsString().equals(Integer.toString(intValue))){
							resultMap.put(attribute.getKey(), attValue.getAsInt());
						} else {
							throw new Exception("It isn't an integer, maybe a long");
						}
					}
					catch(Exception e2){try{resultMap.put(attribute.getKey(), attValue.getAsLong());}
					catch(Exception e1){try{resultMap.put(attribute.getKey(), attValue.getAsDouble());}
					catch(Exception e3){resultMap.put(attribute.getKey(), attValue.getAsString());}					
					}}
				}else if(attValue.isJsonNull()){
						resultMap.put(attribute.getKey(), null);
				}else{
						resultMap.put(attribute.getKey(), attValue.getAsString());
				}
			}
		} else{
			logger.error("Cant parse attributes, JsonObject expected.");
			throw new ArcgisException("Cant parse attributes, JsonObject expected.");
		}
		return resultMap;
	}
}
