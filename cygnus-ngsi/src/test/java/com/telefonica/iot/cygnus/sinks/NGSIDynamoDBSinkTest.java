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
package com.telefonica.iot.cygnus.sinks;

import com.telefonica.iot.cygnus.containers.NotifyContextRequest;
import org.apache.flume.Context;
import org.junit.Test;

/**
 *
 * @author frb
 */
public class NGSIDynamoDBSinkTest {
    
    @Test
    public void dummyTest() {
    } // dummyTest
    
    private NGSIBatch createBatch(long recvTimeTs, String service, String servicePath, String destination,
            NotifyContextRequest.ContextElement contextElement) {
        NGSIEvent groupedEvent = new NGSIEvent(recvTimeTs, service, servicePath, destination, null,
            contextElement);
        NGSIBatch batch = new NGSIBatch();
        batch.addEvent(destination, groupedEvent);
        return batch;
    } // createBatch
    
    private Context createContext(String accessKeyId, String attrPersistence, String enableGrouping, String region,
            String secretAccessKey, String tableType) {
        Context context = new Context();
        context.put("access_key_id", accessKeyId);
        context.put("attr_persistence", attrPersistence);
        context.put("enable_grouping", enableGrouping);
        context.put("region", region);
        context.put("secret_access_key", secretAccessKey);
        context.put("table_type", tableType);
        return context;
    } // createContext
    
} // NGSIDynamoDBSinkTest
