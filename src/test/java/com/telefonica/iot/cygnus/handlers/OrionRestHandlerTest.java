/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
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

package com.telefonica.iot.cygnus.handlers;

import org.apache.flume.Context;
import static org.junit.Assert.*; // this is required by "fail" like assertions
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;

/**
 *
 * @author frb
 */
@RunWith(MockitoJUnitRunner.class)
public class OrionRestHandlerTest {
    
    /**
     * [OrionRestHandler.configure] -------- When not configured, the default values are used for non mandatory
     * parameters.
     */
    @Test
    public void testConfigureNotMandatoryParameters() {
        System.out.println("[OrionRestHandler.configure] -------- When not configured, the default values are used "
                + "for non mandatory parameters");
        OrionRestHandler handler = new OrionRestHandler();
        handler.configure(createContext(null, null, null));
        
        try {
            assertEquals("/notify", handler.getNotificationTarget());
            assertEquals("default", handler.getDefaultService());
            assertEquals("/", handler.getDefaultServicePath());
            System.out.println("[OrionRestHandler.configure] -  OK  - The default configuration values are used");
        } catch (AssertionError e) {
            System.out.println("[OrionRestHandler.configure] - FAIL - The default configuration values are not used");
            throw e;
        } // try catch
    } // testConfigureNotMandatoryParameters
    
    private Context createContext(String notificationTarget, String defaultService, String defaultServicePath) {
        Context context = new Context();
        context.put("notification_target", notificationTarget);
        context.put("default_service", defaultService);
        context.put("default_service_path", defaultServicePath);
        return context;
    } // createContext

} // OrionRestHandlerTest