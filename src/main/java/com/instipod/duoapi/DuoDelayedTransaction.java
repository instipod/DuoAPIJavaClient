package com.instipod.duoapi;

import com.duosecurity.client.Http;
import com.instipod.duoapi.exceptions.DuoRequestFailedException;
import com.instipod.duoapi.exceptions.DuoRequestTimeoutException;
import org.json.JSONObject;

import java.util.logging.Level;

public class DuoDelayedTransaction {
    private String transactionIdentifier;
    private DuoAPIObject apiObject;

    private boolean hasCompleted = false;
    private String finalResult = "";

    public DuoDelayedTransaction(DuoAPIObject apiObject, String transactionIdentifier) {
        this.apiObject = apiObject;
        this.transactionIdentifier = transactionIdentifier;
    }

    public String checkStatus() throws DuoRequestFailedException {
        if (hasCompleted) return finalResult;

        String result = internalCheckStatus();
        if (!result.equalsIgnoreCase("waiting")) {
            hasCompleted = true;
            finalResult = result;
            apiObject.log(Level.FINE, "Duo returned a response for delayed transaction " + transactionIdentifier + ": " + result);
        }

        return result;
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    protected void setResult(String result) {
        hasCompleted = true;
        finalResult = result;
    }

    private String internalCheckStatus() throws DuoRequestFailedException {
        Http request = apiObject.getHttpRequest("GET", "/auth/v2/auth_status", 1);
        request.addParam("txid", transactionIdentifier);

        JSONObject response;
        try {
            response = apiObject.getResponse(request);
        } catch (DuoRequestTimeoutException e) {
            return "waiting";
        }

        try {
            if (!response.getString("stat").equalsIgnoreCase("OK")) throw new Exception("Duo response did not include OK!");
            return response.getJSONObject("response").getString("result");
        } catch (Exception ex) {
            apiObject.log(Level.WARNING, "Unable to query status on delayed transaction " + transactionIdentifier + ", " + ex.getMessage());
            throw new DuoRequestFailedException(ex.getMessage());
        }
    }

    public static DuoDelayedTransaction getDeniedTransaction(DuoAPIObject apiObject) {
        DuoDelayedTransaction ddt = new DuoDelayedTransaction(apiObject, "");
        ddt.setResult("deny");
        return ddt;
    }

    public String getTransactionIdentifier() {
        return transactionIdentifier;
    }
}
