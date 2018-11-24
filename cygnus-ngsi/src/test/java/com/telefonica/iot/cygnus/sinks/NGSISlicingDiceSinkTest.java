package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.errors.CygnusBadConfiguration;
import org.apache.flume.Context;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;

/**
 *
 * @autor joaosimbiose
 */
@RunWith(MockitoJUnitRunner.class)
public class NGSISlicingDiceSinkTest {

    /**
     * Constructor
     */
    public NGSISlicingDiceSinkTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // NGSISlicingDiceSinkTest

    @Test
    public void testConfigureGetConfiguration() {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "false";
        final String dataModel = "dm-by-service-path";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        try {
            Assert.assertEquals(databaseKey, sink.getDatabaseKey());
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "-  OK  - 'database_key=oasdisadnasoi' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "- FAIL - 'database_key=oasdisadnasoi' was not configured");
            throw e;
        } // try catch

        try {
            Assert.assertFalse(sink.isAutoCreate());
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "-  OK  - 'auto_create=false' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "- FAIL - 'auto_create=false' was not configured");
            throw e;
        } // try catch
    } // testConfigureGetConfiguration

    @Test
    public void testConfigureAutoCreateTrue() {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-service-path";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        try {
            Assert.assertTrue(sink.isAutoCreate());
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "-  OK  - 'auto_create=false' was configured");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "- FAIL - 'auto_create=false' was not configured");
            throw e;
        } // try catch
    } // testConfigureAutoCreateTrue

    @Test
    public void testInvalidConfiguration() {
        final String databaseKey = null;
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-service-path";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        try {
            assertTrue(sink.getInvalidConfiguration());
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "-  OK  - 'database_key=null' detected");
        } catch (AssertionError e) {
            System.out.println(getTestTraceHead("[NGSISlicingDiceSink.configure]")
                    + "- FAIL - database_key=null' was not detected");
            throw e;
        } // try catch
    } // testConfigureAutoCreateTrue

    @Test
    public void testBuildDimensionNameOnlyServicePath() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-service-path";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        String servicePath = "/users_a_x_15_$";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        Assert.assertEquals("users-a-x-15--", sink.buildDimensionName(servicePath, entity,
                attribute));
    } // testBuildDimensionNameOnlyServicePath

    @Test
    public void testBuildDimensionNameEntity() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-entity";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        String servicePath = "/users_a_x_15_$";
        String entity = "some_entity_x"; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        Assert.assertEquals("users-a-x-15---some-entity-x", sink.buildDimensionName(
                servicePath, entity, attribute));
    } // testBuildDimensionNameEntity

    @Test
    public void testBuildDimensionNameAttribute() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-attribute";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, null));

        String servicePath = "/users_a_x_15_$";
        String entity = "some_entity_x"; // irrelevant for this test
        String attribute = "some_attribute_%"; // irrelevant for this test
        Assert.assertEquals("users-a-x-15---some-entity-x-some-attribute--", sink.buildDimensionName(
                servicePath, entity, attribute));
    } // testBuildDimensionNameAttribute

    @Test
    public void testBuildDimensionNameEncodingOnlyServicePath() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-service-path";
        final String encoding = "true";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

        String servicePath = "/users_a_x_15_$";
        String entity = null; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        Assert.assertEquals("x002fusersx005fax005fxx005f15x005fx0024", sink.buildDimensionName(servicePath, entity,
                attribute));
    } // testBuildDimensionNameOnlyServicePath

    @Test
    public void testBuildDimensionNameEncodingEntity() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-entity";
        final String encoding = "true";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

        String servicePath = "/users_a_x";
        String entity = "some"; // irrelevant for this test
        String attribute = null; // irrelevant for this test
        Assert.assertEquals("x002fusersx005fax005fxxffffsome", sink.buildDimensionName(
                servicePath, entity, attribute));
    } // testBuildDimensionNameEntity

    @Test
    public void testBuildDimensionNameEncodingAttribute() throws CygnusBadConfiguration {
        final String databaseKey = "oasdisadnasoi";
        final String isAutoCreate = "true";
        final String dataModel = "dm-by-attribute";
        final String encoding = "true";

        final NGSISlicingDiceSink sink = new NGSISlicingDiceSink();
        sink.configure(createContext(databaseKey, isAutoCreate, dataModel, encoding));

        String servicePath = "/users_";
        String entity = "some_"; // irrelevant for this test
        String attribute = "attribute"; // irrelevant for this test
        Assert.assertEquals("x002fusersx005fxffffsomex005fxffffattribute", sink.buildDimensionName(
                servicePath, entity, attribute));
    } // testBuildDimensionNameAttribute

    @Test
    public void testStringEncode() {
        final String[][] tests = new String[][]{
                new String[]{"temperature", "temperature"},
                new String[]{"code_review", "code-review"},
                new String[]{"columns-123", "columns-123"},
                new String[]{"columns_123", "columns-123"},
                new String[]{"some_path", "some-path"}
        };

        // with slash
        for (final String[] test : tests) {
            final String result = NGSISlicingDiceSink.encode("/" + test[0], true, false);
            Assert.assertEquals(test[1], result);
        }

        // without slash
        for (final String[] test : tests) {
            final String result = NGSISlicingDiceSink.encode(test[0], false, true);
            Assert.assertEquals(test[1], result);
        }
    } // testStringEncodeWithoutSlash

    private Context createContext(final String databaseKey, final String autoCreate,
                                  final String dataModel, final String encoding) {
        Context context = new Context();
        context.put("database_key", databaseKey);
        context.put("auto_create", autoCreate);
        context.put("data_model", dataModel);
        context.put("enable_encoding", encoding);
        return context;
    } // createContext
}
