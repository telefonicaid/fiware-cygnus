/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */

package com.telefonica.iot.cygnus.management;

import com.telefonica.iot.cygnus.management.ManagementInterface;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*; // this is required by "when" like functions

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagementInterfaceTest {
    
    // instance to be tested
    private ManagementInterface managementInterface;
    
    // mocks
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    
    // constants
    private final String requestURI = "/version";
    
    /**
     * Sets up tests by creating a unique instance of the tested class, and by defining the behaviour of the mocked
     * classes.
     *  
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // set up the instance of the tested class
        managementInterface = new ManagementInterface(null, null, null);
        
        // set up the behaviour of the mocked classes
        when(mockRequest.getRequestURI()).thenReturn(requestURI);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(System.out));
    } // setUp
    
    /**
     * Test of handle method, of class ManagementInterface.
     */
    @Test
    public void testHandle() {
        System.out.println("Testing ManagementInterface.handle");
        
        try {
            managementInterface.handle(null, mockRequest, mockResponse, 1);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            assertTrue(true);
        } // try catch
    } // testHandle
    
} // ManagementInterfaceTest
