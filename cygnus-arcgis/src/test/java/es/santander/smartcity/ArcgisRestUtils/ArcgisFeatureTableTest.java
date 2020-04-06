package es.santander.smartcity.ArcgisRestUtils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import es.santander.smartcity.exceptions.ArcgisException;
import es.santander.smartcity.model.Feature;

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
                ArcgisBaseTest.getPassword(), ArcgisBaseTest.getGenerateTokenUrl(), false, null);
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

            Feature feature = FeatureTestFactory.getNewOcupacionFeature("Prueba 1 Arcgis.java ", 9991);
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
                        assertTrue(feature2.getAttributes().get("descripcion").equals("Prueba 2 Arcgis.java"));
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
                "https://sags1/arcgis/rest/services/Urbanismo/MobiliarioUrbano_ETRS89/FeatureServer/5", "", "", "",
                false, null);
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
        ArcgisFeatureTable arcgis = new ArcgisFeatureTable(PORTAL_FEATURETABLE_URL, PORTAL_USER, PORTAL_PASSWORD,
                PORTAL_GENERATE_TOKEN_URL, false, null);
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
