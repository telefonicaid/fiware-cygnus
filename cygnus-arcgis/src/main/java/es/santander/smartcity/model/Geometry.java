package es.santander.smartcity.model;

import com.google.gson.JsonObject;

import es.santander.smartcity.exceptions.ArcgisException;

public interface Geometry {	
	static int TYPE_POINT = 1;
	static int TYPE_SHAPE = 2;
		
	public void setSpatialReference(SpatialReference spatialReference);
	public SpatialReference getSpatialReference();
	public void setValue (Geometry g) throws ArcgisException;
	public void setGeometryFromJSON (String json);
	public Object getValue ();
	public String toString();
	public JsonObject toJSON();
	public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException{return null;};
	public int getGeometryType();
}
