package com.telefonica.iot.cygnus.backends.ckan.model;

import com.google.gson.JsonElement;

import java.util.ArrayList;

public class DataStore {

    private String resource_id;
    private ArrayList<JsonElement> fields;
    private String force;

    public  DataStore(){}

    public ArrayList<JsonElement> getFields() {
        return fields;
    }

    public void setFields(ArrayList<JsonElement> fields) {
        this.fields = fields;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }
}
