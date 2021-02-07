package com.instipod.duoapi;

import com.duosecurity.client.Http;
import com.instipod.duoapi.exceptions.DuoRequestFailedException;
import com.instipod.duoapi.exceptions.DuoRequestTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

public class DuoCodeCapableDevice extends DuoDevice {
    public DuoCodeCapableDevice(DuoAPIObject apiObject, String deviceIdentifier, String capabilityIdentifier, String name) {
        super(apiObject, deviceIdentifier, capabilityIdentifier, name);
    }

    public boolean challenge(String username, String ipAddress) {
        apiObject.log(Level.FINE, "Sending a Duo code challenge for " + getCapabilityIdentifier() + " on device " + getDeviceIdentifier() + ".");

        if (getCapabilityIdentifier().equalsIgnoreCase("sms")) {
            //send the sms
            Http pushRequest = apiObject.getHttpRequest("POST", "/auth/v2/auth");
            pushRequest.addParam("username", username);
            pushRequest.addParam("ipaddr", ipAddress);
            pushRequest.addParam("factor", "sms");
            pushRequest.addParam("device", getDeviceIdentifier());

            try {
                JSONObject response = apiObject.getResponse(pushRequest);
                if (!response.getString("stat").equalsIgnoreCase("OK")) {
                    apiObject.log(Level.WARNING, "Unable to send SMS challenge, duo replied with error: " + response.toString());
                    throw new DuoRequestFailedException("Duo API did not return OK!");
                }
                //this request will always return deny if the codes are sent
                return true;
            } catch (DuoRequestTimeoutException e) {
                apiObject.log(Level.WARNING, "Unable to send SMS challenge, duo did not reply before timeout.");
                return false;
            } catch (DuoRequestFailedException e) {
                apiObject.log(Level.WARNING, "Unable to send SMS challenge, " + e.getMessage());
                return false;
            } catch (JSONException e) {
                apiObject.log(Level.WARNING, "Unable to send SMS challenge, received malformed JSON back!");
                return false;
            }
        }
        //the other capabilities (mobile_otp, token, etc.) do not require challenge
        return true;
    }

    public boolean checkResponse(String username, String ipAddress, String passcode) {
        Http pushRequest = apiObject.getHttpRequest("POST", "/auth/v2/auth");
        pushRequest.addParam("username", username);
        pushRequest.addParam("ipaddr", ipAddress);
        pushRequest.addParam("factor", "passcode");
        pushRequest.addParam("passcode", passcode);

        try {
            JSONObject response = apiObject.getResponse(pushRequest);
            if (!response.getString("stat").equalsIgnoreCase("OK")) {
                apiObject.log(Level.WARNING, "Unable to validate code, duo replied with error: " + response.toString());
                throw new DuoRequestFailedException("Duo API did not return OK! " + response.toString());
            }

            String result = response.getJSONObject("response").getString("result");
            if (result.equalsIgnoreCase("allow")) {
                apiObject.log(Level.FINE, "Duo accepted code entry for user " + username + "!");
            } else {
                apiObject.log(Level.FINE, "Duo rejected code entry for user " + username + "!");
            }

            return (result.equalsIgnoreCase("allow"));
        } catch (DuoRequestTimeoutException e) {
            apiObject.log(Level.WARNING, "Unable to validate code, duo did not reply before timeout!");
            return false;
        } catch (DuoRequestFailedException e) {
            apiObject.log(Level.WARNING, "Unable to validate code, " + e.getMessage());
            return false;
        } catch (JSONException e) {
            apiObject.log(Level.WARNING, "Unable to validate code, could not validate JSON from Duo!");
            return false;
        }
    }
}
