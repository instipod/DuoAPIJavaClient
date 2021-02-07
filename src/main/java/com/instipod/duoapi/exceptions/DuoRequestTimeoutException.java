package com.instipod.duoapi.exceptions;

public class DuoRequestTimeoutException extends Exception {
    public DuoRequestTimeoutException() {
        super("The API request to Duo exceeded the specified timeout value!");
    }
}
