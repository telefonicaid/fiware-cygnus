/**
 * 
 */
package es.santander.smartcity.model;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.santander.smartcity.exceptions.ArcgisException;

/**
 * @author dmartinez
 *
 */
public class Field {
	protected static final Logger logger = Logger.getLogger(Feature.class);
	
	protected String name;
	protected String alias;
	protected GisAttributeType type;
	protected boolean unique;
	protected boolean nullable;

	/**
	 * 
	 */
	public Field() {
		name = "";
		type = GisAttributeType.STRING;
		unique = false;
		nullable = false;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the type
	 */
	public GisAttributeType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(GisAttributeType type) {
		this.type = type;
	}

	/**
	 * @return the unique
	 */
	public boolean isUnique() {
		return unique;
	}

	/**
	 * @param unique the unique to set
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * @param nullable the nullable to set
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	
	public static Field createInstanceFromJson(String jsonStr) throws ArcgisException{
		
		JsonObject json;
		try{
			JsonParser parser = new JsonParser();
	        json = parser.parse(jsonStr).getAsJsonObject();
		}catch(Exception e){
			throw new ArcgisException("Cant parse field from JSON: " + e.getMessage());
		}
        
        return createInstanceFromJson(json);
	}
	
	/**
	 * 
	 * @param json
	 * @return
	 * @throws ArcgisException
	 */
	public static Field createInstanceFromJson(JsonObject json) throws ArcgisException{
		Field field = new Field();
		try{
			logger.debug("Parsing Field name - " + json.get("name"));
			field.setName(json.get("name").getAsString());
		
			logger.debug("Parsing Field type - " + json.get("type"));
			String type = json.get("type").getAsString();
			field.setType(GisAttributeType.fromString(type));
			
			logger.debug("Parsing Field alias - " + json.get("alias"));
			field.setAlias(json.get("alias").getAsString());
			
			if (json.has("nullable")){
				logger.debug("Parsing Field nullable - " + json.get("nullable"));
				field.setNullable(json.get("nullable").getAsBoolean());
			}else {
				logger.debug("WARN: Field doesn't have nullable attribute, seting nullable=false.");
				field.setNullable(false);
			}
	//			field.setEditable(json.get("").getAsBoolean());
			
		}catch (Exception e){
			logger.error("Can't cast Attributes from Json. " + json);
			throw new ArcgisException("Can't cast Attributes from Json, " + json);
		}
		return field;
	}
	
	
}
