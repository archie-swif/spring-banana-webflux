package com.banana;

public class ResourceWithStatus {
    public String status;
    public String resource;

    public ResourceWithStatus() {
    }

    public ResourceWithStatus(String status, String resource) {
        this.status = status;
        this.resource = resource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
