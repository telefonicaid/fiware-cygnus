/**
 * Copyright 2018 Telefonica Investigación y Desarrollo, S.A.U
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
 *
 * Authorship: TIS Inc.
 *
 */
package com.telefonica.iot.cygnus.backends.elasticsearch;

import com.telefonica.iot.cygnus.backends.http.HttpBackend;
import com.telefonica.iot.cygnus.backends.http.JsonResponse;
import com.telefonica.iot.cygnus.errors.CygnusPersistenceError;
import com.telefonica.iot.cygnus.errors.CygnusRuntimeError;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

/**
 * Implementation of Elasticsearch Backend.
 *
 * @author Nobuyuki Matsui (TIS Inc.)
 */
public class ElasticsearchBackendImpl extends HttpBackend implements ElasticsearchBackend {
    private static final CygnusLogger LOGGER = new CygnusLogger(ElasticsearchBackendImpl.class);

    /**
     * Constructor.
     *
     * @param elasticsearchHost
     * @param elasticsearchPort
     * @param ssl
     * @param maxConns
     * @param maxConnsPerRoute
     */
    public ElasticsearchBackendImpl(String elasticsearchHost, String elasticsearchPort, boolean ssl, int maxConns, int maxConnsPerRoute) {
        super(elasticsearchHost, elasticsearchPort, ssl, false, null, null, null, null, maxConns, maxConnsPerRoute);
    } // ElasticsearchBackendImpl

    /**
     * store data to Elasticsearch using REST API.
     *
     * @param index index name of Elasticsearch, not {@code null}
     * @param type type name of Elasticsearch, not {@code null}
     * @param data  data to be stored, like below (recvTimeTs is used to make unique id), not {@code null}:
     *   [
     *     {
     *       recvTimeTs: "1234567890",
     *       data: {
     *         recvTime: "1970-01-15T06:56:07.890Z",
     *         entityId: "Room1",
     *         entityType: "Room",
     *         attrName: "temperature",
     *         attrType: "number",
     *         attrValue: "26.5",
     *         attrMetadata: []
     *       }
     *     },
     *     {
     *       recvTimeTs: "1234568001",
     *       data: {
     *         recvTime: "1970-01-15T06:56:08.001Z",
     *         entityId: "Room1",
     *         entityType: "Room",
     *         attrName: "temperature",
     *         attrType: "number",
     *         attrValue: "26.6",
     *         attrMetadata: []
     *       }
     *     }
     *   ]
     * @return response of {@code doRequest}
     * @throws com.telefonica.iot.cygnus.errors.CygnusPersistenceError
     * @throws com.telefonica.iot.cygnus.errors.CygnusRuntimeError
     */
    @Override
    public JsonResponse bulkInsert(String index, String type, List<Map<String, String>> data) throws CygnusPersistenceError, CygnusRuntimeError {
        if (StringUtils.isBlank(index) || StringUtils.isBlank(type) || data == null) {
            throw new CygnusPersistenceError("invalid arguments (index=" + index + ", type=" + type + ", data=" + data + ")");
        } // if
        String relativeURL = "/" + index + "/" + type + "/_bulk";

        String jsonLines = "";
        StringEntity entity;
        try {
            for (Map<String, String> elem : data) {
                String edata = elem.get("data");
                String erecvTimeTs = elem.get("recvTimeTs");
                if (StringUtils.isBlank(edata) || StringUtils.isBlank(erecvTimeTs)) {
                    throw new CygnusPersistenceError("invalid data format (data=" + data + ")");
                }
                byte[] bytes = MessageDigest.getInstance("MD5").digest(edata.getBytes(StandardCharsets.UTF_8));
                String hash = DatatypeConverter.printHexBinary(bytes);
                jsonLines += String.format("{\"index\":{\"_id\":\"%s-%s\"}}\n", erecvTimeTs, hash);
                jsonLines += String.format("%s\n", edata);
            } // for
            entity = new StringEntity(jsonLines);
        } catch (NoSuchAlgorithmException e) {
            throw new CygnusPersistenceError("Could not create id (data=" + data + "), rootCause=" + e.toString() + ")");
        } catch (UnsupportedEncodingException e) {
            throw new CygnusPersistenceError("Could not create StringEntity (data=" + data + "), rootCause=" + e.toString() + ")");
        } // try-catch
        LOGGER.debug("bulk insert (index=" + index + ", type=" + type + ", jsonLines=" + jsonLines + ")");

        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-type", "application/json"));
        headers.add(new BasicHeader("Accept", "application/json"));

        JsonResponse response = doRequest("POST", relativeURL, true, headers, entity);
        if (response.getStatusCode() == 200) {
            LOGGER.debug("Successfully bulk inserted (index=" + index + ", type=" + type + ", jsonLines=" + jsonLines + ")");
            return response;
        } else {
            throw new CygnusPersistenceError("Could not insert (index=" + index + ", type=" + type + ", jsonLines=" + jsonLines + ")");
        } // if
    } // bulkInsert
} // ElasticsearchBackendImpl
