package com.instipod.duoapi.exceptions;

public class DuoRequestFailedException extends Exception {
    public DuoRequestFailedException(String message) {
        super("Duo Request Failed: " + message);
    }
}
