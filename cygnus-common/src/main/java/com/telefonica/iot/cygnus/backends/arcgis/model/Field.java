/**
 * Copyright 2014-2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.backends.arcgis.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * @author dmartinez
 *
 */
public class Field {
    private static final CygnusLogger LOGGER = new CygnusLogger(Field.class);
    
    private static final String NULLABLE_TAG = "nullable";
    private static final String ALIAS_TAG = "alias";
    private static final String TYPE_TAG = "type";
    private static final String NAME_TAG = "name";


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
     * @param the name to set
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
     * @param the alias to set
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
     * @param the type to set
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
     * @param the unique to set
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * @return is nullable
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * @param the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Factory method.
     * 
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
            LOGGER.debug("Parsing Field name - " + json.get(NAME_TAG));
            field.setName(json.get(NAME_TAG).getAsString());

            LOGGER.debug("Parsing Field type - " + json.get(TYPE_TAG));
            String type = json.get(TYPE_TAG).getAsString();
            field.setType(GisAttributeType.fromString(type));

            LOGGER.debug("Parsing Field alias - " + json.get(ALIAS_TAG));
            field.setAlias(json.get(ALIAS_TAG).getAsString());

            if (json.has(NULLABLE_TAG)) {
                LOGGER.debug("Parsing Field nullable - " + json.get(NULLABLE_TAG));
                field.setNullable(json.get(NULLABLE_TAG).getAsBoolean());
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
