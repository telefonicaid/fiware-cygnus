/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
package com.telefonica.iot.cygnus.interceptors;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.telefonica.iot.cygnus.containers.NameMappings;
import com.telefonica.iot.cygnus.containers.NameMappings.AttributeMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.EntityMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.ServiceMapping;
import com.telefonica.iot.cygnus.containers.NameMappings.ServicePathMapping;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextAttribute;
import com.telefonica.iot.cygnus.containers.NotifyContextRequest.ContextElement;
import com.telefonica.iot.cygnus.log.CygnusLogger;
import com.telefonica.iot.cygnus.utils.CommonConstants;
import com.telefonica.iot.cygnus.utils.JsonUtils;
import com.telefonica.iot.cygnus.utils.NGSIConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

/**
 *
 * @author frb
 */
public class NGSINameMappingsInterceptor implements Interceptor {

    private static final CygnusLogger LOGGER = new CygnusLogger(NGSINameMappingsInterceptor.class);
    private final String nameMappingsConfFile;
    private final boolean invalidConfiguration;
    private NameMappings nameMappings;
    private PeriodicalNameMappingsReader periodicalNameMappingsReader;

    /**
     * Constructor.
     * 
     * @param nameMappingsConfFile
     * @param invalidConfiguration
     */
    public NGSINameMappingsInterceptor(String nameMappingsConfFile, boolean invalidConfiguration) {
        this.nameMappingsConfFile = nameMappingsConfFile;
        this.invalidConfiguration = invalidConfiguration;
    } // NGSINameMappingsInterceptor

    @Override
    public void initialize() {
        if (!invalidConfiguration) {
            loadNameMappings();
            LOGGER.info("[nmi] Name mappings loaded");

            // Create and start a periodical name mappings reader
            periodicalNameMappingsReader = new PeriodicalNameMappingsReader(30000);
            periodicalNameMappingsReader.start();
            LOGGER.info("[nmi] Periodical name mappings reader started");
        } // if
    } // initialize

    @Override
    public Event intercept(Event event) {
        if (invalidConfiguration) {
            return event;
        } // if

        LOGGER.debug("[nmi] Event intercepted, id=" + event.hashCode());

        // Casting to NGSIEvent
        NGSIEvent ngsiEvent = (NGSIEvent) event;

        // Get the original headers
        Map<String, String> headers = event.getHeaders();

        // Get some original header values
        String originalService = headers.get(CommonConstants.HEADER_FIWARE_SERVICE);
        String originalServicePath = headers.get(CommonConstants.HEADER_FIWARE_SERVICE_PATH);

        // Create the mapped NotifyContextRequest
        ImmutableTriple<String, String, ContextElement> map = doMap(originalService, originalServicePath,
                ngsiEvent.getOriginalCE());
        LOGGER.debug("[nmi] Mapped ContextElement: " + map.getRight().toString());

        // Add the mapped ContextElement to the NGSIEvent
        ngsiEvent.setMappedCE(map.getRight());

        // Add the bytes version of the mapped ContextElement to event's body
        byte[] originalCEBytes = ngsiEvent.getBody();
        byte[] mappedCEBytes = map.getRight().toString().getBytes();
        byte[] newBody = new byte[originalCEBytes.length + mappedCEBytes.length];
        System.arraycopy(originalCEBytes, 0, newBody, 0, originalCEBytes.length);
        System.arraycopy(mappedCEBytes, 0, newBody, originalCEBytes.length, mappedCEBytes.length);
        ngsiEvent.setBody(newBody);
        LOGGER.debug("[nmi] New body: " + new String(newBody));

        // Add the mapped service and service path to the headers
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE, map.getLeft());
        LOGGER.debug("[nmi] Header added to NGSI event (" + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE + ": "
                + map.getLeft() + ")");
        headers.put(NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH, map.getMiddle());
        LOGGER.debug("[nmi] Header added to NGSI event (" + NGSIConstants.FLUME_HEADER_MAPPED_SERVICE_PATH + ": "
                + map.getMiddle() + ")");

        // Return the intercepted event
        LOGGER.debug("[nmi] Event put in the channel, id=" + ngsiEvent.hashCode());
        return ngsiEvent;
    } // intercept

    @Override
    public List<Event> intercept(List<Event> events) {
        if (invalidConfiguration) {
            return events;
        } else {
            List<Event> interceptedEvents = new ArrayList<>(events.size());

            for (Event event : events) {
                Event interceptedEvent = intercept(event);
                interceptedEvents.add(interceptedEvent);
            } // for

            return interceptedEvents;
        } // if else
    } // intercept

    @Override
    public void close() {
    } // close

    /**
     * Builder class for this new interceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private boolean invalidConfiguration;
        private String nameMappingsConfFile;

        @Override
        public void configure(Context context) {
            nameMappingsConfFile = context.getString("name_mappings_conf_file");

            if (nameMappingsConfFile == null) {
                invalidConfiguration = true;
                LOGGER.error("[nmi] Invalid configuration (name_mappings_conf_file = null) -- Must be configured");
            } else if (nameMappingsConfFile.length() == 0) {
                invalidConfiguration = true;
                LOGGER.error("[nmi] Invalid configuration (nameMappingsConfFile = ) -- Cannot be empty");
            } else {
                LOGGER.info("[nmi] Reading configuration (nameMappingsConfFile=" + nameMappingsConfFile + ")");
            } // if else
        } // configure

        @Override
        public Interceptor build() {
            return new NGSINameMappingsInterceptor(nameMappingsConfFile, invalidConfiguration);
        } // build

        protected boolean getInvalidConfiguration() {
            return invalidConfiguration;
        } // getInvalidConfiguration

    } // Builder

    /**
     * Class in charge or periodically reading the NGSINameMappingsInterceptor
     * configuration file.
     */
    private class PeriodicalNameMappingsReader extends Thread {

        private final CygnusLogger logger = new CygnusLogger(PeriodicalNameMappingsReader.class);
        private final int interval;
        private long lastModified;
        private boolean stop;

        PeriodicalNameMappingsReader(int interval) {
            this.interval = interval;
            this.lastModified = 0;
            this.stop = false;
        } // PeriodicalNameMappingsReader

        @Override
        public void run() {
            while (!stop) {
                // Check if the configuration has changed
                File groupingRulesFile = new File(nameMappingsConfFile);
                long modified = groupingRulesFile.lastModified();

                if (lastModified != 0 && modified == lastModified) {
                    logger.debug("[nmi] The configuration has not changed");

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        logger.error(
                                "[nmi] There was a problem with the checking interval. Details: " + e.getMessage());
                    } // try catch

                    continue;
                } // if

                // Reload the name mappings
                lastModified = modified;
                loadNameMappings();

                // Sleep the configured interval of time
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error("There was a problem with the checking interval. Details: " + e.getMessage());
                } // try catch
            } // while
        } // run

        public void signalForStop() {
            this.stop = true;
        } // signalForStop

    } // PeriodicalNameMappingsReader

    private void loadNameMappings() {
        // Read the Json string from the configuration file
        String jsonStr;

        try {
            jsonStr = JsonUtils.readJsonFile(nameMappingsConfFile);
            LOGGER.debug("[nmi] Reading name mappings, Json read: " + jsonStr);
        } catch (Exception e) {
            LOGGER.error("[nmi] Runtime error (" + e.getMessage() + ")");
            nameMappings = null;
            return;
        } // try catch

        loadNameMappings(jsonStr);
    } // loadNameMappings

    /**
     * Loads the Name Mappings given a Json string. It is protected since it
     * only can be used by this class and test classes.
     * 
     * @param jsonStr
     */
    protected void loadNameMappings(String jsonStr) {
        if (jsonStr == null) {
            LOGGER.debug("[nmi] Reding name mappings, no file to read");
            nameMappings = null;
            return;
        } // if

        // Parse the Json string
        Gson gson = new Gson();

        try {
            nameMappings = gson.fromJson(jsonStr, NameMappings.class);
            LOGGER.debug("[nmi] Reading attribute mappings, Json parsed");
        } catch (JsonIOException e) {
            LOGGER.error("[nmi] Runtime error (" + e.getMessage() + ")");
            nameMappings = null;
            return;
        } catch (JsonSyntaxException e) {
            LOGGER.error("[nmi] Runtime error (" + e.getMessage() + ")");
            nameMappings = null;
            return;
        } // try catch

        if (nameMappings != null) {
            // Check if any of the mappings is not valid, e.g. some field is missing
            nameMappings.purge();
            LOGGER.debug("[nmi] Reading name mappings, Json purged");

            // Pre-compile the regular expressions
            nameMappings.compilePatterns();
            LOGGER.debug("[nmi] Reading name mappings, regular expressions pre-compiled");
        }
    } // loadNameMappings

    /**
     * Applies the mappings to the input NotifyContextRequest object.
     * 
     * @param originalService
     * @param originalServicePath
     * @param originalCE
     * @return The input NotifyContextRequest object with maps applied
     */
    public ImmutableTriple<String, String, ContextElement> doMap(String originalService, String originalServicePath,
            ContextElement originalCE) {
        if (nameMappings == null) {
            return new ImmutableTriple(originalService, originalServicePath, originalCE);
        } // if

        // Triple to be returned
        String newService = originalService;
        String newServicePath = originalServicePath;
        ContextElement newCE = originalCE.deepCopy();

        // Map the service
        ServiceMapping serviceMapping = null;

        for (ServiceMapping sm : nameMappings.getServiceMappings()) {
            serviceMapping = sm;

            if (!serviceMapping.getOriginalServicePattern().matcher(originalService).matches()) {
                serviceMapping = null;
                continue;
            } // if

            LOGGER.debug("[nmi] FIWARE service found: " + originalService);

            if (serviceMapping.getNewService() != null) {
                newService = originalService.replaceAll(serviceMapping.getOriginalServicePattern().toString(),
                                                        serviceMapping.getNewService());
                LOGGER.debug("[nmi] FIWARE new service obtained: " + newService);
            } // if

            break;
        } // for

        if (serviceMapping == null) {
            LOGGER.debug("[nmi] FIWARE service not found: " + originalService);
            return new ImmutableTriple(newService, newServicePath, newCE);
        } // if

        // Map the service path
        ServicePathMapping servicePathMapping = null;

        for (ServicePathMapping spm : serviceMapping.getServicePathMappings()) {
            LOGGER.debug("[nmi] checking with servicePathMappings: " + spm.toString());
            if (!spm.getOriginalServicePathPattern().matcher(originalServicePath).matches()) {
                continue;
            } // if

            LOGGER.debug("[nmi] FIWARE service path found: " + originalServicePath);

            for (EntityMapping em : spm.getEntityMappings()) {
                LOGGER.debug("[nmi] checking em: " + em.toString());
                // check type
                if (em.getOriginalEntityType() != null) {
                    if (!em.getOriginalEntityTypePattern().matcher(newCE.getType()).matches()) {
                        continue;
                    } else {
                        if (spm.getNewServicePath() != null) {
                            newServicePath = originalServicePath.replaceAll(spm.getOriginalServicePathPattern().toString(),
                                                                            spm.getNewServicePath());
                        }
                        LOGGER.debug("[nmi] FIWARE new service path obtained: " + newServicePath);
                        servicePathMapping = spm;
                        break;
                    }
                } else {
                    if (spm.getNewServicePath() != null) {
                        newServicePath = originalServicePath.replaceAll(spm.getOriginalServicePathPattern().toString(),
                                                                        spm.getNewServicePath());
                    }
                    LOGGER.debug("[nmi] FIWARE new service path obtained: " + newServicePath);
                    servicePathMapping = spm;
                    break;
                }
            }
            if (servicePathMapping != null) {
                break;
            }
        } // for

        if (servicePathMapping == null) {
            LOGGER.debug("[nmi] FIWARE service path not found: " + originalServicePath);
            return new ImmutableTriple(newService, newServicePath, newCE);
        } // if

        String originalEntityId = newCE.getId();
        String originalEntityType = newCE.getType();
        String newEntityId = originalEntityId;
        String newEntityType = originalEntityType;
        EntityMapping entityMapping = null;

        for (EntityMapping em : servicePathMapping.getEntityMappings()) {
            entityMapping = em;
            LOGGER.debug("[nmi] checking with entityMapping: " + entityMapping.toString());

            // check if match by Type
            if (entityMapping.getOriginalEntityType() != null) {
                if (!entityMapping.getOriginalEntityTypePattern().matcher(originalEntityType).matches()) {
                    LOGGER.debug("[nmi] not matches type");
                    continue;
                } else {
                    LOGGER.debug("[nmi] " + entityMapping.getOriginalEntityType() + " matches " + newCE.getType());
                }
            }
            // check if match by Id
            if (entityMapping.getOriginalEntityId() != null) {
                if (!entityMapping.getOriginalEntityIdPattern().matcher(originalEntityId).matches()) {
                    LOGGER.debug("[nmi] not matches entityId");
                    continue;
                } else {
                    LOGGER.debug("[nmi] " + entityMapping.getOriginalEntityId() + " matches " + newCE.getId());
                }
            }
            if (!entityMapping.getOriginalEntityIdPattern().matcher(originalEntityId).matches() ||
                !entityMapping.getOriginalEntityTypePattern().matcher(originalEntityType).matches()) {
                LOGGER.debug("[nmi] not matches both type and entityId");
                entityMapping = null;
                continue;
            } // if

            LOGGER.debug("[nmi] Entity found: " + originalEntityId + ", " + originalEntityType);

            if (entityMapping.getNewEntityId() != null) {
                LOGGER.debug("[nmi] IdPattern : " + entityMapping.getOriginalEntityIdPattern().toString());
                newEntityId = originalEntityId.replaceAll(entityMapping.getOriginalEntityIdPattern().toString(),
                        entityMapping.getNewEntityId());
                LOGGER.debug("[nmi] newEntityId : " + newEntityId);
            } // if

            if (entityMapping.getNewEntityType() != null) {
                newEntityType = entityMapping.getNewEntityType();
            } // if

            break;
        } // for

        if (entityMapping == null) {
            LOGGER.debug("[nmi] Entity not found: " + originalEntityId + ", " + originalEntityType);
            return new ImmutableTriple(newService, newServicePath, newCE);
        } // if

        newCE.setId(newEntityId);
        newCE.setType(newEntityType);

        for (ContextAttribute newCA : newCE.getAttributes()) {
            LOGGER.debug("[nmi] checking with CA: " + newCA.toString());
            String originalAttributeName = newCA.getName();
            String originalAttributeType = newCA.getType();
            String newAttributeName = originalAttributeName;
            String newAttributeType = originalAttributeType;
            AttributeMapping attributeMapping = null;

            for (AttributeMapping am : entityMapping.getAttributeMappings()) {
                attributeMapping = am;
                LOGGER.debug("[nmi] checking with attributeMapping: " + attributeMapping.toString());

                // Check attribute type
                if (attributeMapping.getOriginalAttributeType() != null) {
                    if (!attributeMapping.getOriginalAttributeTypePattern().matcher(originalAttributeType).matches()) {
                        LOGGER.debug("[nmi] not matches attribute type");
                        continue;
                    } else {
                        LOGGER.debug("[nmi] " + attributeMapping.getOriginalAttributeType() + " matches ");
                    }
                }
                // Check attribute name
                if (attributeMapping.getOriginalAttributeName() != null) {
                    if (!attributeMapping.getOriginalAttributeNamePattern().matcher(originalAttributeName).matches()) {
                        LOGGER.debug("[nmi] not matches attribute name");
                        continue;
                    } else {
                        LOGGER.debug("[nmi] " + attributeMapping.getOriginalAttributeName() + " matches ");
                    }
                }

                if (!attributeMapping.getOriginalAttributeNamePattern().matcher(originalAttributeName).matches() ||
                    !attributeMapping.getOriginalAttributeTypePattern().matcher(originalAttributeType).matches()) {
                    LOGGER.debug("[nmi] not matches both attribute type and name");
                    attributeMapping = null;
                    continue;
                } // if

                LOGGER.debug("[nmi] Attribute found: " + originalAttributeName + ", " + originalAttributeType);

                if (attributeMapping.getNewAttributeName() != null) {
                    newAttributeName = attributeMapping.getNewAttributeName();
                } // if

                if (attributeMapping.getNewAttributeType() != null) {
                    newAttributeType = attributeMapping.getNewAttributeType();
                } // if

                break;
            } // for

            if (attributeMapping == null) {
                LOGGER.debug("[nmi] Attribute not found: " + originalAttributeName + ", " + originalAttributeType);
                continue;
            } // if

            newCA.setName(newAttributeName);
            newCA.setType(newAttributeType);
            LOGGER.debug("[nmi] newCA: " + newCA.toString());
        } // for
        LOGGER.debug("[nmi] newCE: " + newCE.toString());
        return new ImmutableTriple(newService, newServicePath, newCE);
    } // map

} // NGSINameMappingsInterceptor
