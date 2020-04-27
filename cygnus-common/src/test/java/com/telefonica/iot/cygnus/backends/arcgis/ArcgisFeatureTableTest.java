/**
 * Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
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

package com.telefonica.iot.cygnus.backends.arcgis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.backends.arcgis.exceptions.ArcgisException;
import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;
import com.telefonica.iot.cygnus.backends.arcgis.restutils.ArcgisFeatureTable;

/**
 * 
 * @author dmartinez
 *
 */
public class ArcgisFeatureTableTest implements ArcgisBaseTest {

    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
    }

    private ArcgisFeatureTable connectArcgis() {
        return new ArcgisFeatureTable(ArcgisBaseTest.getFeatureUrl(), ArcgisBaseTest.getUser(),
                ArcgisBaseTest.getPassword(), ArcgisBaseTest.getGenerateTokenUrl(), false);
    }

    /**
     * 
     */
    @Test
    public void arcgisAddBatchTest() {
        System.out.println("----------------  arcgisAddBatchTest");
        try {
            ArcgisFeatureTable arcgis = connectArcgis();
            arcgis.setBatchSize(2);
            arcgis.setBatchAction(ArcgisFeatureTable.ADD_ACTION);

            Feature feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 1 Arcgis.java ");
            arcgis.addToBatch(feature);
            feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 2 Arcgis.java ");
            arcgis.addToBatch(feature);

            assertTrue(true);
        } catch (Exception e) {
            System.err.println("ERROR Cant add entities " + e.getCause() + "\n" + e.getMessage());
            fail(e.getCause() + "\n" + e.getMessage());
        }
    }

    /**
     * 
     */
    @Test
    public void arcgisAddUpdateBatchTest() {
        System.out.println("----------------  arcgisAddUpdateBatchTest");
        String uniqueField = "IDEXTERNO";

        try {
            ArcgisFeatureTable arcgis = connectArcgis();
            arcgis.setBatchSize(2);
            arcgis.setUniqueField(uniqueField);
            arcgis.setBatchAction(ArcgisFeatureTable.ADD_UPDATE_ACTION);

            Feature feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 1 Arcgis.java ",
                    9991);
            arcgis.addToBatch(feature);
            feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 2 Arcgis.java", 9992);
            arcgis.addToBatch(feature);

            feature = FeatureTestFactory.getNewOcupacionFeature("modificado ", 9991);
            arcgis.addToBatch(feature);
            feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 3 Arcgis.java ", 9993);
            arcgis.addToBatch(feature);

            arcgis.flushBatch();

            feature = FeatureTestFactory.getNewOcupacionFeature("modificado ", 9993);
            arcgis.addToBatch(feature);

            arcgis.flushBatch();

            List<Feature> list = arcgis.queryFeatures(uniqueField + " IN (9991,9992,9993)");
            assertTrue("El número de features insertado es correcto", list.size() == 3);
            for (Feature feature2 : list) {
                Integer id = (Integer) feature2.getAttributes().get(uniqueField);
                switch (id) {
                    case 9991:
                    case 9993:
                        assertTrue(feature2.getAttributes().get("descripcion").equals("modificado"));
                        break;
                    case 9992:
                        assertTrue(feature2.getAttributes().get("descripcion")
                                .equals("Prueba 2 Arcgis.java"));
                        break;
                    default:
                        fail("Unexpected Feature");
                }
            }

            assertTrue(true);

        } catch (Exception e) {
            System.err.println("ERROR processing entities " + e.getCause() + "\n" + e.getMessage());
            fail(e.getCause() + "\n" + e.getMessage());
        }
    }

    /**
     * 
     */
    @Test
    public void arcgisGetFeatures() {
        System.out.println("----------------  arcgisGetFeatures");
        ArcgisFeatureTable arcgis = new ArcgisFeatureTable(
                "https://sags1/arcgis/rest/services/Urbanismo/MobiliarioUrbano_ETRS89/FeatureServer/5",
                "", "", "", false);
        List<Feature> resultList;
        try {
            resultList = arcgis.queryFeatures(arcgis.getUniqueIdField() + ">0");

            // List<Feature> resultList = arcgis.queryFeatures("ID_ENTITY>0");
            assertTrue("No results.", resultList != null && resultList.size() > 0);
        } catch (ArcgisException e) {
            fail(e.getMessage());
        }
    }

    /**
     * 
     */
    @Test
    public void arcgisGetSecuredFeatures() {
        System.out.println("----------------  arcgisGetSecuredFeatures");
        ArcgisFeatureTable arcgis = new ArcgisFeatureTable(PORTAL_FEATURETABLE_URL, PORTAL_USER,
                PORTAL_PASSWORD, PORTAL_GENERATE_TOKEN_URL, false);
        List<Feature> resultList;
        try {
            resultList = arcgis.queryFeatures(arcgis.getUniqueIdField() + ">0");

            // List<Feature> resultList = arcgis.queryFeatures("ID_ENTITY>0");
            assertTrue("No results.", resultList != null && resultList.size() > 0);
        } catch (ArcgisException e) {
            fail(e.getMessage());
        }
    }

}
