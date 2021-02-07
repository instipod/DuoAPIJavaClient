package com.instipod.duoapi;

import com.duosecurity.client.Http;

import java.io.UnsupportedEncodingException;

public class APICredentials {
    private String apiHostname;
    private String integrationKey;
    private String secretKey;

    public APICredentials() {
        apiHostname = "";
        integrationKey = "";
        secretKey = "";
    }

    public void setCredentials(String apiHostname, String integrationKey, String secretKey) {
        this.apiHostname = apiHostname;
        this.integrationKey = integrationKey;
        this.secretKey = secretKey;
    }

    public String getApiHostname() {
        return apiHostname;
    }

    public Http signRequest(Http request) throws UnsupportedEncodingException {
        request.signRequest(integrationKey, secretKey);
        return request;
    }
}
