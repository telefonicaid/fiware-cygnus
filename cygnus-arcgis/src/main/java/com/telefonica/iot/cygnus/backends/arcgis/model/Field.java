/**
 * 
 */
package com.telefonica.iot.cygnus.backends.arcgis.model;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.model.Field;
import com.telefonica.iot.cygnus.backends.arcgis.model.GisAttributeType;

/**
 * @author dmartinez
 *
 */
public class Field {
    protected static final Logger LOGGER = Logger.getLogger(Feature.class);

    private String name;
    private String alias;
    private GisAttributeType type;
    private boolean unique;
    private boolean nullable;

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
     * @param name
     *            the name to set
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
     * @param alias
     *            the alias to set
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
     * @param type
     *            the type to set
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
     * @param unique
     *            the unique to set
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
     * @param nullable
     *            the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Factory method.
     * @param jsonStr
     * @return
     * @throws ArcgisException
     */
    public static Field createInstanceFromJson(String jsonStr) throws ArcgisException {

        JsonObject json;
        try {
            JsonParser parser = new JsonParser();
            json = parser.parse(jsonStr).getAsJsonObject();
        } catch (Exception e) {
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
    public static Field createInstanceFromJson(JsonObject json) throws ArcgisException {
        Field field = new Field();
        try {
            LOGGER.debug("Parsing Field name - " + json.get("name"));
            field.setName(json.get("name").getAsString());

            LOGGER.debug("Parsing Field type - " + json.get("type"));
            String type = json.get("type").getAsString();
            field.setType(GisAttributeType.fromString(type));

            LOGGER.debug("Parsing Field alias - " + json.get("alias"));
            field.setAlias(json.get("alias").getAsString());

            if (json.has("nullable")) {
                LOGGER.debug("Parsing Field nullable - " + json.get("nullable"));
                field.setNullable(json.get("nullable").getAsBoolean());
            } else {
                LOGGER.debug("WARN: Field doesn't have nullable attribute, seting nullable=false.");
                field.setNullable(false);
            }
            // field.setEditable(json.get("").getAsBoolean());

        } catch (Exception e) {
            LOGGER.error("Can't cast Attributes from Json. " + json);
            throw new ArcgisException("Can't cast Attributes from Json, " + json);
        }
        return field;
    }

}
