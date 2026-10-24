package com.example.forecast;

import jakarta.xml.ws.WebFault;

/**
 * Thrown when a country code is missing, malformed or not covered by the forecast data.
 * Exposed to SOAP clients as a fault; the message is always generic.
 */
@WebFault(name = "InvalidCountryFault", targetNamespace = "http://forecast.example.com/")
public class InvalidCountryException extends Exception {

    private static final long serialVersionUID = 1L;

    private final InvalidCountryFault faultInfo;

    public InvalidCountryException(String message) {
        super(message);
        this.faultInfo = new InvalidCountryFault(message);
    }

    public InvalidCountryException(String message, InvalidCountryFault faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public InvalidCountryFault getFaultInfo() {
        return faultInfo;
    }
}
