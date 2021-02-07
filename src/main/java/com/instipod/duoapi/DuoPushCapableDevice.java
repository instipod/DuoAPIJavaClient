package com.instipod.duoapi;

import com.duosecurity.client.Http;
import com.instipod.duoapi.exceptions.DuoRequestFailedException;
import com.instipod.duoapi.exceptions.DuoRequestTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

public class DuoPushCapableDevice extends DuoDevice {
    public DuoPushCapableDevice(DuoAPIObject apiObject, String deviceIdentifier, String capabilityIdentifier, String name) {
        super(apiObject, deviceIdentifier, capabilityIdentifier, name);
    }

    public DuoDelayedTransaction push(String username, String appName, String ipAddress) {
        Http pushRequest = apiObject.getHttpRequest("POST", "/auth/v2/auth");
        pushRequest.addParam("username", username);
        pushRequest.addParam("ipaddr", ipAddress);
        pushRequest.addParam("factor", getCapabilityIdentifier());
        pushRequest.addParam("async", "1");
        pushRequest.addParam("device", getDeviceIdentifier());
        pushRequest.addParam("display_username", username);
        pushRequest.addParam("pushinfo", "from=" + appName);

        try {
            apiObject.log(Level.FINE, "Sending a Duo Push for user " + username + ".");
            JSONObject response = apiObject.getResponse(pushRequest);

            if (!response.getString("stat").equalsIgnoreCase("OK")) {
                apiObject.log(Level.WARNING, "Unable to send Duo Push, Duo returned an error: " + response.toString());
                throw new DuoRequestFailedException("Duo API did not return OK!");
            }

            return new DuoDelayedTransaction(apiObject, response.getJSONObject("response").getString("txid"));
        } catch (DuoRequestTimeoutException e) {
            apiObject.log(Level.WARNING, "Unable to send Duo Push, Duo did not reply before timeout!");
            return null;
        } catch (DuoRequestFailedException e) {
            apiObject.log(Level.WARNING, "Unable to send Duo Push, request error: " + e.getMessage());
            return null;
        } catch (JSONException e) {
            apiObject.log(Level.WARNING, "Unable to send Duo Push, unable to decode JSON from Duo!");
            return null;
        }
    }
}
