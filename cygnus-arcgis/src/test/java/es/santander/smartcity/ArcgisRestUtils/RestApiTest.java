/**
 * 
 */
package es.santander.smartcity.ArcgisRestUtils;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import es.santander.smartcity.exceptions.ArcgisException;
import es.santander.smartcity.http.HttpResponse;

/**
 * @author dmartinez
 *
 */
public class RestApiTest {
	
	@Before
	public void setUp () {
		BasicConfigurator.configure();
	}
	

	@Test
	public void simpleGetTest() {
		String urlRequest = "http://www.google.es";

		HashMap<String,String> params = new HashMap<String, String>();
				
		try {
			HttpResponse response = RestApi.httpGet(urlRequest, params);
			System.out.println(response.toString());
			Assert.assertTrue(response.getResponseCode()==200);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void ParameterTest() {

		String urlRequest = "https://sags1/arcgis/rest/services/Policia";

		HashMap<String,String> params = new HashMap<String, String>();
		params.put("f", "pjson");
		
		try {
			HttpResponse response = RestApi.httpGet(urlRequest, params);
			System.out.println(response.toString());
			Assert.assertTrue(response.getResponseCode()==200);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void checkResponseTest (){
		HttpResponse response = new HttpResponse(200, "{OK}");
		RestApi.checkHttpResponse(response);
		
		Assert.assertTrue("Response should be ok.",response.isSuccessful());
	}
	
	@Test
	public void checkMultiResponseTest (){		
		HttpResponse response = new HttpResponse(200
				, "{\"addResults\":[{\"objectId\":null,\"uniqueId\":null,\"globalId\":\"B7131F8B-CCE4-4D77-BA75-CD474D8F05EF\",\"success\":false,\"error\":{\"code\":1000,\"description\":\"Cannot insert duplicate key row in object 'user_19420.OcupacionDummy_POINT_LAYER' with unique index 'Id_Index'. The duplicate key value is (8).\\r\\nThe statement has been terminated.\"}}]}");
		RestApi.checkHttpResponse(response);
		
		Assert.assertTrue("Response should be error.",response.hasError());
	}
	
	@Test
	public void finallyTest () throws Exception{
		try{
			System.out.println ("Running try");
			throw new Exception("Test Exception.");
		}catch(ArcgisException e){
			System.out.println ("Catching exception: " + e);
		}finally{
			System.out.println ("Finally...................................") ;
			Assert.assertTrue(true);
		}
	}

}
