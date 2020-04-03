/**
 * 
 */
package es.santander.smartcity.model;

import es.santander.smartcity.exceptions.ArcgisException;

/**
 * @author dmartinez
 *
 */
public enum GisAttributeType {

	DATE("Date"),
	INTEGER("Integer"),
	NUMBER("Number"),
	BOOLEAN("Boolean"),
	STRING("String"),
	OID("OBJECTID"),
	GID("GlobalID"),
	GEOMETRY("Geometry");
	
	private String strValue;
	
	GisAttributeType(String value){
		this.strValue = value;
	}
	
	public String toString(){
		return strValue;
	}
	
	public static GisAttributeType fromString(String strType) throws ArcgisException{
		strType = strType.replace("esriFieldType", "");
		switch(strType){
		case "Date":
			return GisAttributeType.DATE;
		case "Integer":
		case "SmallInteger":
			return GisAttributeType.INTEGER;
		case "Number":
		case "Double":
			return GisAttributeType.NUMBER;
		case "Boolean":
			return GisAttributeType.BOOLEAN;
		case "String":
			return GisAttributeType.STRING;
		case "OBJECTID":
		case "OID":
			return GisAttributeType.OID;
		case "GlobalID":
			return GisAttributeType.GID;
		case "Geometry":
			return GisAttributeType.GEOMETRY;
		default: throw new ArcgisException("Invalid string type: " + strType);
		}
	}
}
