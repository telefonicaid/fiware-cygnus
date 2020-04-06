package com.telefonica.iot.cygnus.backends.arcgis.restutils;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;

import junit.framework.Assert;

/**
 * 
 * @author dmartinez
 *
 */
public class FeatureTest {

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
    }

    @Test
    public void getObjectIdTest() {
        System.out.println("----------------  getObjectIdTest");
        Feature noId = FeatureTestFactory.getNewOcupacionFeature("Sin id");
        Feature withId = FeatureTestFactory.getUpdatedOcupacionFeature(12, "con id");

        System.out.println("Entidad sin id: " + noId.toString());

        try {
            System.out.println("Con id retorna: " + withId.getObjectId());
        } catch (ArcgisException e) {
            fail(e.getMessage());
        }

        try {
            System.out.println("Sin id retorna: " + noId.getObjectId());
            fail("Encuentra id cuando no debería haberlo.");
        } catch (ArcgisException e) {
            assertTrue("Sin id sale por excepción.", true);
        }

        assertTrue("ok.", true);
    }

    @Test
    public void createInstanceFromJson() {
        System.out.println("----------------  createInstanceFromJson");
        Feature feature = FeatureTestFactory.getUpdatedOcupacionFeature(2, "Prueba");
        try {
            JsonObject json = feature.toJson();

            Feature copia = Feature.createInstanceFromJson(json);
            if (copia.getObjectId() == feature.getObjectId()) {
                System.out.println("Success -  " + copia.toString());
                assertTrue("Instancia creada con éxito.", true);
            } else {
                System.err.println("la Instancia no coincide con el Json dado.");
                fail("la Instancia no coincide con el Json dado.");
            }
        } catch (ArcgisException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void getSetObjectId() {
        System.out.println("----------------  getSetObjectId");

        Feature feature = new Feature();
        try {
            feature.setObjectId(255);
            assertTrue("getObjectId() doesn't match", feature.getObjectId() == 255);
            assertTrue("OBJECTID not properly saved", ((Long) feature.getAttributes().get("OBJECTID")) == 255);
        } catch (ArcgisException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void createInstanceFromJson2() {
        System.out.println("----------------  createInstanceFromJson2");
        String jsonResponse = FeatureTestFactory.getGetFeaturesResponse();
        JsonParser parser = new JsonParser();

        try {
            JsonObject json = parser.parse(jsonResponse).getAsJsonObject();

            JsonArray features = json.get("features").getAsJsonArray();

            for (JsonElement featureJson : features) {
                Feature feature = Feature.createInstanceFromJson(featureJson.getAsJsonObject());
                if (feature == null)
                    fail("Feature, Cant be created, " + featureJson.toString());
            }

            Assert.assertTrue(true);
        } catch (ArcgisException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
