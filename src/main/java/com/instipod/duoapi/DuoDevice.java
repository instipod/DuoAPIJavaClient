package com.instipod.duoapi;

public class DuoDevice {
    protected DuoAPIObject apiObject;
    protected String deviceIdentifier;
    protected String name;
    protected String capabilityIdentifier;

    public DuoDevice(DuoAPIObject apiObject, String deviceIdentifier, String capabilityIdentifier, String name) {
        this.apiObject = apiObject;
        this.deviceIdentifier = deviceIdentifier;
        this.capabilityIdentifier = capabilityIdentifier;
        this.name = name;
    }

    public DuoAPIObject getApiObject() {
        return apiObject;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getName() {
        if (name == null || name.equalsIgnoreCase("")) {
            return getDeviceIdentifier();
        } else {
            return name;
        }
    }

    public String getCapabilityIdentifier() {
        return capabilityIdentifier;
    }

    @Override
    public String toString() {
        if (this instanceof DuoCodeCapableDevice) {
            return "DuoCodeCapableDevice - " + getCapabilityIdentifier() + " - " + getDeviceIdentifier() + " - " + getName();
        } else if (this instanceof DuoPushCapableDevice) {
            return "DuoPushCapableDevice - " + getCapabilityIdentifier() + " - " + getDeviceIdentifier() + " - " + getName();
        } else {
            return "Unknown Duo Device";
        }
    }
}
