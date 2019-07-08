/**
 * 
 */
package com.telefonica.iot.cygnus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.telefonica.iot.cygnus.log.CygnusLogger;

import es.santander.smartcity.arcgisutils.Entity;

/**
 * @author joelcamus
 *
 */
public class EntityArcGisUtils {

    private static final CygnusLogger LOGGER = new CygnusLogger(PropertyUtils.class);
    
    /**
     * 
     * @param bodyJSON
     * @return
     */
    public List<Entity> createEntities(JSONArray jsonArray, String service, String servicePath) {
        LOGGER.debug("init createEntities(jsonArray --> " + jsonArray + ")");
        List<Entity> listEntity = new ArrayList<Entity>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            listEntity.add(createEntity(jsonObject, service, servicePath));
        } // for
        LOGGER.debug("listEntity--> " + listEntity);
        return listEntity;

    } // createEntities

    /**
     * Create Automatic entity.
     * 
     * @param jsonObject
     * @return
     */
    public Entity createEntity(JSONObject jsonObject, String service, String servicePath) {
        try {
            double latitude = 0, longitude = 0;
            Map<String, Object> attributes = new HashMap<String, Object>();
            for (Object keyO : jsonObject.keySet()) {
                String key = (String) keyO;
                // Get a id or type of json
                if (key.equals("type") || key.equals("id")) {
                    if (key.equals("id")) {
                        attributes.put(key, jsonObject.get(key).toString());
                    } else {
                        attributes.put(key + "Entity", jsonObject.get(key).toString());
                    }
                    // Get a coordenates of json
                } else if (key.equals("location")) {
                    JSONObject jsonObjectV1 = JsonUtils.parseJsonString(jsonObject.get(key).toString());
                    String[] a = JsonUtils.parseJsonString(jsonObjectV1.get("value").toString()).get("coordinates")
                            .toString().replace("[", "").replace("]", "").split(",");
                    latitude = Double.parseDouble(a[1]);
                    longitude = Double.parseDouble(a[0]);
                } else {
                    try {
                        JSONObject jsonObjectV1 = JsonUtils.parseJsonString(jsonObject.get(key).toString());
                        // If this method throws a ParseException, the value
                        // isn't a json object
                        getAttribute(key, key, jsonObjectV1, attributes);

                    } catch (ParseException e) {
                        // Add value which isn't JSON
                        attributes.put(key, DataUtils.getStringToObject(jsonObject.get(key).toString()));
                    } catch (Exception e) {
                        // Add value which isn't JSON
                        attributes.put(key, DataUtils.getStringToObject(jsonObject.get(key).toString()));
                    }
                }
            } // for
              // create entity with latitud an longitud
            Entity e = Entity.createPointEntity(latitude, longitude);
            // add other attributes
            attributes.put("service", service);
            attributes.put("servicePath", servicePath);
            e.setAttributes(attributes);

            return e;
        } catch (ParseException e1) {
            LOGGER.error(e1.getMessage());
            return null;
        } // try catch

    } // createEntity

    /**
     * Get attribute.
     * 
     * @param key
     * @param keyFinal
     * @param jsonAttribute
     * @param attributes
     */
    private void getAttribute(String key, String keyFinal, JSONObject jsonAttribute, Map<String, Object> attributes) {
        for (Object key2 : jsonAttribute.keySet()) {
            if ((!((String) key2).equals("metadata")) && (!((String) key2).equals("type"))) {
                try {
                    JSONObject jsonObjectV1 = JsonUtils.parseJsonString(jsonAttribute.get(key2).toString());
                    keyFinal += "_" + key2;
                    getAttribute((String) key2, keyFinal, jsonObjectV1, attributes);
                } catch (ParseException e) {
                    // Add value which isn't JSON
                    attributes.put((keyFinal + "_" + key2).replace("_value", ""),
                            DataUtils.getStringToObject(jsonAttribute.get(key2).toString()));
                } catch (Exception e) {
                    // Add value which isn't JSON
                    attributes.put((keyFinal + "_" + key2).replace("_value", ""),
                            DataUtils.getStringToObject(jsonAttribute.get(key2).toString()));
                } // try catch
            } // if
        } // for
    } // getAttribute
}
