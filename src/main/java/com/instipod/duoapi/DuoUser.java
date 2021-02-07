package com.instipod.duoapi;

import com.duosecurity.client.Http;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Level;

public class DuoUser {
    private DuoAPIObject apiObject;
    private String username;
    private String ipAddress;

    private boolean hasData = false;
    private boolean userExists = false;
    private String enrollURL = "";
    private String defaultResponse = "auth";
    private ArrayList<DuoPushCapableDevice> pushDevices;
    private ArrayList<DuoCodeCapableDevice> codeDevices;

    public DuoUser(DuoAPIObject apiObject, String username, String ipAddress) {
        this.apiObject = apiObject;
        this.username = username;
        this.ipAddress = ipAddress;
        pushDevices = new ArrayList<DuoPushCapableDevice>();
        codeDevices = new ArrayList<DuoCodeCapableDevice>();
    }

    public boolean refresh() {
        apiObject.log(Level.FINE, "Querying Duo for information about user " + username + ".");
        Http request = apiObject.getHttpRequest("POST", "/auth/v2/preauth");
        request.addParam("username", username);
        request.addParam("ipaddr", ipAddress);
        try {
            JSONObject response = apiObject.getResponse(request);
            if (response.getString("stat").equalsIgnoreCase("OK")) {
                hasData = true;
                userExists = true;
                defaultResponse = response.getJSONObject("response").getString("result");
                apiObject.log(Level.FINE, "Query with user " + username + " received result " + defaultResponse);
                if (response.getJSONObject("response").has("devices"))
                    updateDeviceList(response.getJSONObject("response").getJSONArray("devices"));
                if (response.getJSONObject("response").has("enroll_portal_url"))
                    enrollURL = response.getJSONObject("response").getString("enroll_portal_url");
            } else {
                apiObject.log(Level.WARNING, "Duo did not reply with success to user query: " + response.toString());
                return false;
            }
            return true;
        } catch (Exception e) {
            apiObject.log(Level.WARNING, "Exception while querying Duo about user: " + e.getMessage());
            return false;
        }
    }

    public String getDefaultAction() {
        return defaultResponse;
    }

    public boolean exists() {
        if (!hasData) return false;
        return userExists;
    }

    public boolean hasData() {
        return hasData;
    }

    public boolean shouldEnroll() {
        return (getDevices().size() == 0);
    }

    public boolean mustEnroll() {
        return (getDefaultAction().equalsIgnoreCase("enroll"));
    }

    public boolean canEnrollHere() {
        return (!enrollURL.equalsIgnoreCase(""));
    }

    public String getEnrollURL() {
        return enrollURL;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasCapability(String capability) {
        ArrayList<DuoDevice> devices = getDevicesOfCapability(capability);
        return (devices.size() > 0);
    }

    public ArrayList<DuoCodeCapableDevice> getCodeDevices() {
        return codeDevices;
    }

    public ArrayList<DuoPushCapableDevice> getPushDevices() {
        return pushDevices;
    }

    public ArrayList<DuoDevice> getDevices() {
        ArrayList<DuoDevice> allDevices = new ArrayList<DuoDevice>();

        for (DuoPushCapableDevice device : getPushDevices()) {
            allDevices.add((DuoDevice)device);
        }
        for (DuoCodeCapableDevice device : getCodeDevices()) {
            allDevices.add((DuoDevice)device);
        }

        return allDevices;
    }

    public ArrayList<DuoDevice> getDevicesOfCapability(String capability) {
        ArrayList<DuoDevice> matchingDevices = new ArrayList<DuoDevice>();
        for (DuoDevice device : getDevices()) {
            if (device.getCapabilityIdentifier().equalsIgnoreCase(capability)) {
                matchingDevices.add(device);
            }
        }
        return matchingDevices;
    }

    public DuoDevice getFirstDevice(String[] preference) {
        for (String s : preference) {
            ArrayList<DuoDevice> matchingPref = getDevicesOfCapability(s);
            for (DuoDevice d : matchingPref) {
                return d;
            }
        }
        return null;
    }

    private void updateDeviceList(JSONArray devices) {
        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject device = devices.getJSONObject(i);
                String name;
                if (device.has("name")) {
                    name = device.getString("name");
                } else {
                    name = "";
                }

                if (device.getString("type").equalsIgnoreCase("phone")) {
                    //check capabilities
                    JSONArray capabilities = device.getJSONArray("capabilities");

                    for (int c = 0; c < capabilities.length(); c++) {
                        String capability = capabilities.getString(c);
                        if (capability.equalsIgnoreCase("push")) {
                            pushDevices.add(new DuoPushCapableDevice(apiObject, device.getString("device"), "push", name));
                        }
                        if (capability.equalsIgnoreCase("phone")) {
                            pushDevices.add(new DuoPushCapableDevice(apiObject, device.getString("device"), "phone", name));
                        }

                        if (capability.equalsIgnoreCase("sms")) {
                            codeDevices.add(new DuoCodeCapableDevice(apiObject, device.getString("device"), "sms", name));
                        }
                        if (capability.equalsIgnoreCase("mobile_otp")) {
                            codeDevices.add(new DuoCodeCapableDevice(apiObject, device.getString("device"), "mobile_otp", name));
                        }
                    }
                } else if (device.getString("type").equalsIgnoreCase("token")) {
                    //code only here
                    codeDevices.add(new DuoCodeCapableDevice(apiObject, device.getString("device"), "token", name));
                }
            } catch (Exception ex) {
                //todo?
            }
        }
        apiObject.log(Level.FINE, "Retrieved " + getDevices().size() + " devices for user " + username + " from Duo.");
    }
}
