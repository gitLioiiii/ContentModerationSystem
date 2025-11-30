package com.example.demo.utils;

import java.util.HashMap;
import java.util.Map;

public class ResultTemplate {

    private Boolean status  = true;

    private String message  = "OK";

    private Map<String, Object> payload;

    public ResultTemplate() {
        // this.status     = true;
        // this.message    = "OK";
        this.payload    = new HashMap<>();
    }

    public ResultTemplate(Map<String, Object> payload) {
        // this.status     = true;
        // this.message    = "OK";
        this.payload    = payload;
    }

    public ResultTemplate(Boolean status, String message, Map<String, Object> payload) {
        this.status     = status;
        this.message    = message;
        this.payload    = payload;
    }

    public Boolean getStatus() {
        return status;
    }

    public ResultTemplate setStatus(Boolean status) {
        this.status = status;

        return this;
    }

    public String getMessage() {
        return message;
    }

    public ResultTemplate setMessage(String message) {
        this.message = message;

        return this;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public ResultTemplate setPayload(Map<String, Object> payload) {
        this.payload = payload;

        return this;
    }

    public ResultTemplate putPayload(String key, Object value) {
        this.payload.put(key, value);

        return this;
    }

}
