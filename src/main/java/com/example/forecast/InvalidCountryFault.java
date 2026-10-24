package com.example.forecast;

/**
 * Fault detail bean serialized into the SOAP fault. Message is always generic.
 */
public class InvalidCountryFault {

    private String message;

    public InvalidCountryFault() {
    }

    public InvalidCountryFault(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
