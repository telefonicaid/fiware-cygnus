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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.telefonica.iot.cygnus.backends.arcgis.model.Feature;

/**
 * @author dmartinez
 *
 */
public abstract class FeatureTestFactory {

    /**
     * 
     * @param name
     * @return
     */
    public static Feature getNewOcupacionFeature(String name) {
        return getNewOcupacionFeature(name, null);
    }

    /**
     * 
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewOcupacionFeature(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);

        Feature feature = Feature.createPointFeature(43, -3);
        feature.setAttributes(attributes);
        return feature;
    }

    /**
     * 
     * @param objectId
     * @param name
     * @return
     */
    public static Feature getUpdatedOcupacionFeature(int objectId, String name) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("OBJECTID", objectId);
        attributes.put("IDEXTERNO", null);
        attributes.put("RAZONSOCIAL", "Razon social updated");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 1);
        attributes.put("FINI", -2209161600000L);
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 1);
        attributes.put("EXCFESTIVOS", 1);
        attributes.put("PRESENCIAPOLICIAL", 1);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad updated");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "updated");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", -2209161600000L);
        attributes.put("CANTIDADOCUPADA", null);
        attributes.put("DESCRIPCION", name + "descripcion");

        Feature feature = Feature.createPointFeature(43, -3);
        feature.setAttributes(attributes);
        return feature;
    }

    /**
     *
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewPolyLineFeature(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"paths\": [ [ [-97.06138, 32.837], [-97.06133, 33.836], [-98.2, 34.834], [-97, 40] ] ] }";
        Feature feature = Feature.createPolyLineFeature(jsonString);
        feature.setAttributes(attributes);
        return feature;
    }

    public static Feature getNewPolyLineFeature2(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"paths\": [ [ [-97.06138, 32.837], [-97.06133, 33.836], [-98.2, 34.834], [-97, 40] ] ] }";
        Feature feature = null;
        try {
            feature = Feature.createInstanceFromJson(jsonString);
            feature.setAttributes(attributes);
        } catch (Exception e) {
            System.out.println("Exception");
            System.out.println(e.getClass().getSimpleName() + "  " + e.getMessage());
        }
        return feature;
    }

    /**
     *
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewPolygonFeature(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"rings\": [ [ [-97.06138,32.837,35.1,4.8], [-97.06133,32.836,35.2,4.1], [-97.06124,32.834,35.3,4.2], [-97.06138,32.837,35.1,4.8] ], [ [-97.06326,32.759,35.4],  [-97.06298,32.755,35.5], [-97.06153,32.749,35.6], [-97.06326,32.759,35.4] ] ] }";
        Feature feature = Feature.createPolygonFeature(jsonString);
        feature.setAttributes(attributes);
        return feature;
    }

    /**
     *
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewPolygonFeature2(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"rings\": [ [ [-97.06138,32.837,35.1,4.8], [-97.06133,32.836,35.2,4.1], [-97.06124,32.834,35.3,4.2], [-97.06138,32.837,35.1,4.8] ], [ [-97.06326,32.759,35.4],  [-97.06298,32.755,35.5], [-97.06153,32.749,35.6], [-97.06326,32.759,35.4] ] ] }";
        Feature feature = null;
        try {
            feature = Feature.createInstanceFromJson(jsonString);
            feature.setAttributes(attributes);
        } catch (Exception e) {
            System.out.println("Exception");
            System.out.println(e.getClass().getSimpleName() + "  " + e.getMessage());
        }
        return feature;
    }

    /**
     *
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewMultiPointFeature(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"points\": [ [-97.06138, 32.837], [-97.06133, 33.836], [-98.2, 34.834], [-97, 40] ] }";
        Feature feature = Feature.createMultiPointFeature(jsonString);
        feature.setAttributes(attributes);
        return feature;
    }

    /**
     *
     * @param description
     * @param externalId
     * @return
     */
    public static Feature getNewMultiPointFeature2(String description, Integer externalId) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        attributes.put("IDEXTERNO", externalId);
        attributes.put("DESCRIPCION", description);
        attributes.put("RAZONSOCIAL", "Razon social");
        attributes.put("NUMEROPOSTAL", null);
        attributes.put("TIPOOCUPACION", 0);
        attributes.put("FINI", new Date());
        attributes.put("UNIDADMEDIDA", null);
        attributes.put("EXCSABDOM", 0);
        attributes.put("EXCFESTIVOS", 0);
        attributes.put("PRESENCIAPOLICIAL", 0);
        attributes.put("REVISADO", 0);
        attributes.put("IDACTIVIDAD", 0);
        attributes.put("ACTIVIDAD", "actividad");
        attributes.put("IDCLASE", 0);
        attributes.put("CLASE", "clase");
        attributes.put("IDESTADO", 0);
        attributes.put("ESTADO", "estado");
        attributes.put("CALLE", "calle");
        attributes.put("FFIN", new GregorianCalendar());
        attributes.put("CANTIDADOCUPADA", null);
        String jsonString = "{ \"points\": [ [-97.06138, 32.837], [-97.06133, 33.836], [-98.2, 34.834], [-97, 40] ] }";
        Feature feature = null;
        try {
            feature = Feature.createInstanceFromJson(jsonString);
            feature.setAttributes(attributes);
        } catch (Exception e) {
            System.out.println("Exception");
            System.out.println(e.getClass().getSimpleName() + "  " + e.getMessage());
        }
        return feature;
    }

    /**
     * 
     * @return
     */
    public static String getGetFeaturesResponse() {
        return "{  \"objectIdFieldName\" : \"OBJECTID\",   \"uniqueIdField\" :   {    \"name\" : \"OBJECTID\",     \"isSystemMaintained\" : true  },   \"globalIdFieldName\" : \"GlobalID\",   \"geometryType\" : \"esriGeometryPoint\",   \"spatialReference\" : {    \"wkid\" : 4326,     \"latestWkid\" : 4326  },   \"fields\" : [    {      \"name\" : \"OBJECTID\",       \"type\" : \"esriFieldTypeOID\",       \"alias\" : \"OBJECTID\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"GlobalID\",       \"type\" : \"esriFieldTypeGlobalID\",       \"alias\" : \"GlobalID\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 38,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"CreationDate\",       \"type\" : \"esriFieldTypeDate\",       \"alias\" : \"CreationDate\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 8,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Creator\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Creator\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 128,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"EditDate\",       \"type\" : \"esriFieldTypeDate\",       \"alias\" : \"EditDate\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 8,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Editor\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Editor\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 128,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"RazonSocial\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Raz\u00F3n Social\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"NomCalle\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Calle\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"NumCalle\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"N\u00FAmero\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"TipoOcupaSid\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Tipo Ocupaci\u00F3n\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Inicio\",       \"type\" : \"esriFieldTypeDate\",       \"alias\" : \"Inicio\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 0,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Fin\",       \"type\" : \"esriFieldTypeDate\",       \"alias\" : \"Fin\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 0,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"TipoMedida\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"IdMedida\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"CantidadOcupada\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Cantidad Ocupada\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"ExclSabDom\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Excluido Sab/Dom\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"ExclFestivos\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Excluido Festivos\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"PresenciaPolicial\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Presencia Policial\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"RevisadoOk\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Revisado\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"IdActividad\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"IdActividad\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"IdClase\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"IdClase\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"DesClase\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Clase\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"IdEstado\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"IdEstado\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"DesEstado\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Estado\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"DesActividad\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Actividad\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 256,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Descripcion\",       \"type\" : \"esriFieldTypeString\",       \"alias\" : \"Descripci\u00F3n\",       \"sqlType\" : \"sqlTypeOther\",       \"length\" : 4000,       \"domain\" : null,       \"defaultValue\" : null    },     {      \"name\" : \"Id\",       \"type\" : \"esriFieldTypeInteger\",       \"alias\" : \"Id\",       \"sqlType\" : \"sqlTypeOther\",       \"domain\" : null,       \"defaultValue\" : null,       \"description\" : \"{\\\"value\\\":\\\"\\\",\\\"fieldValueType\\\":\\\"uniqueIdentifier\\\"}\"    }  ],   \"features\" : [    {      \"attributes\" : {        \"OBJECTID\" : 3,         \"GlobalID\" : \"4422b88f-ae2d-4294-8f68-16733b6bc59e\",         \"CreationDate\" : 1584455698656,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455698656,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres\",         \"Id\" : 3      }    },     {      \"attributes\" : {        \"OBJECTID\" : 4,         \"GlobalID\" : \"9506356d-4bd0-437d-a9e8-113d7ca2f4d8\",         \"CreationDate\" : 1584455698656,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455698656,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 4\",         \"Id\" : 4      }    },     {      \"attributes\" : {        \"OBJECTID\" : 5,         \"GlobalID\" : \"b182d56c-d7e6-4aa1-bd82-9a24db16911a\",         \"CreationDate\" : 1584455698656,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455698656,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 5\",         \"Id\" : 5      }    },     {      \"attributes\" : {        \"OBJECTID\" : 6,         \"GlobalID\" : \"64ba4112-7f16-4bed-9392-6cf7e0b2a4e3\",         \"CreationDate\" : 1584455698656,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455698656,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 6\",         \"Id\" : 6      }    },     {      \"attributes\" : {        \"OBJECTID\" : 7,         \"GlobalID\" : \"ecf01c4d-6e56-45ba-8e2f-6cf549e7d5c2\",         \"CreationDate\" : 1584455698656,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455698656,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 7\",         \"Id\" : 7      }    },     {      \"attributes\" : {        \"OBJECTID\" : 8,         \"GlobalID\" : \"bb8cddee-cbd7-4092-b2ce-b1f6b381a832\",         \"CreationDate\" : 1584455700213,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455700213,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 8\",         \"Id\" : 8      }    },     {      \"attributes\" : {        \"OBJECTID\" : 9,         \"GlobalID\" : \"07977dfe-235d-43d1-b39b-e5b7647571bc\",         \"CreationDate\" : 1584455700213,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455700213,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 9\",         \"Id\" : 9      }    },     {      \"attributes\" : {        \"OBJECTID\" : 10,         \"GlobalID\" : \"4735d8f2-0442-4bcc-9540-ed8caf1acad7\",         \"CreationDate\" : 1584455700213,         \"Creator\" : \"SantanderSmartCity\",         \"EditDate\" : 1584455700213,         \"Editor\" : \"SantanderSmartCity\",         \"RazonSocial\" : null,         \"NomCalle\" : null,         \"NumCalle\" : null,         \"TipoOcupaSid\" : null,         \"Inicio\" : null,         \"Fin\" : null,         \"TipoMedida\" : null,         \"CantidadOcupada\" : null,         \"ExclSabDom\" : null,         \"ExclFestivos\" : null,         \"PresenciaPolicial\" : null,         \"RevisadoOk\" : null,         \"IdActividad\" : null,         \"IdClase\" : null,         \"DesClase\" : null,         \"IdEstado\" : null,         \"DesEstado\" : null,         \"DesActividad\" : null,         \"Descripcion\" : \"DescTres 10\",         \"Id\" : 10      }    }  ]}\r\n\t";
    }

}
