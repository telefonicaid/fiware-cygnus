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
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;

/**
 * 
 * @author dmartinez
 *
 */
public interface Geometry {
    
    static int TYPE_POINT = 1;
    static int TYPE_SHAPE = 2;

    /**
     * Sets spatial reference value.
     * @param spatialReference
     */
    public void setSpatialReference(SpatialReference spatialReference);

    /**
     * Gets spatial reference value.
     * @return
     */
    public SpatialReference getSpatialReference();

    /**
     * Set geometry.
     * @param g
     * @throws ArcgisException
     */
    public void setValue(Geometry g) throws ArcgisException;

    /**
     * Sets geometry from geo-json string.
     * @param json
     */
    public void setGeometryFromJSON(String json);

    /**
     * Get geometry.
     * @return
     */
    public Object getValue();

    /**
     * parse to string.
     * @return
     */
    public String toString();

    /**
     * parse to Json.
     * @return
     */
    public JsonObject toJSON();

    /**
     * Factory method.
     * @param json
     * @return
     * @throws ArcgisException
     */
    public static Geometry createInstanceFromJson(JsonObject json) throws ArcgisException {
        return null;
    };

    /**
     * Gets geometry type.
     * @return
     */
    public int getGeometryType();
}
