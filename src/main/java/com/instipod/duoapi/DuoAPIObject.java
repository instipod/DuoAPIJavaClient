package com.instipod.duoapi;

import com.duosecurity.client.Http;
import com.instipod.duoapi.exceptions.DuoRequestFailedException;
import com.instipod.duoapi.exceptions.DuoRequestTimeoutException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DuoAPIObject {
    private APICredentials apiCredentials;
    private Logger logger;
    protected int apiTimeout;
    private boolean fineLogging = false;

    public DuoAPIObject(String apiHostname, String integrationKey, String secretKey, int apiTimeout) {
        apiCredentials = new APICredentials();
        apiCredentials.setCredentials(apiHostname, integrationKey, secretKey);
        logger = Logger.getLogger("DuoAPIObject");
        apiTimeout = apiTimeout;
    }

    public String getApiHostname() {
        return apiCredentials.getApiHostname();
    }

    protected Http getHttpRequest(String method, String url, int timeout) {
        Http request = new Http(method, getApiHostname(), url, timeout);
        return request;
    }

    protected Http getHttpRequest(String method, String url) {
        return getHttpRequest(method, url, apiTimeout);
    }

    public void enableFineLogging() {
        fineLogging = true;
    }

    protected void log(Level level, String message) {
        if (level.equals(Level.FINE)) {
            if (!fineLogging) return;
        }
        logger.log(level, message);
    }

    protected JSONObject getResponse(Http request) throws DuoRequestTimeoutException, DuoRequestFailedException {
        try {
            apiCredentials.signRequest(request);
        } catch (UnsupportedEncodingException ex) {
            log(Level.SEVERE, "The JRE does not support the encoding required for Duo signages!");
            throw new DuoRequestFailedException("The encoding is not supported!");
        }

        try {
            String jsonResponse = request.executeRequestRaw();
            return new JSONObject(jsonResponse);
        } catch (Exception ex) {
            if (ex.getMessage().equalsIgnoreCase("timeout")) {
                throw new DuoRequestTimeoutException();
            } else {
                log(Level.WARNING, "Duo API request failed at HTTP level: " + ex.getMessage());
                throw new DuoRequestFailedException(ex.getMessage());
            }
        }
    }

    public DuoUser getUser(String username, String ipAddress) {
        return new DuoUser(this, username, ipAddress);
    }
}
