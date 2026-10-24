package com.example.forecast;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Code-first SOAP endpoint returning an economic forecast score for a country.
 */
@WebService(serviceName = "EconomicForecastService",
        portName = "EconomicForecastPort",
        targetNamespace = "http://forecast.example.com/")
public class EconomicForecastService {

    private static final Logger LOG = Logger.getLogger(EconomicForecastService.class.getName());

    private final ForecastCalculator calculator = new ForecastCalculator();

    /**
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return forecast score between 0 and 100
     * @throws InvalidCountryException (SOAP fault) if the code is invalid or unsupported
     */
    @WebMethod(operationName = "getForecastScore")
    @WebResult(name = "score")
    public double getForecastScore(@WebParam(name = "countryCode") String countryCode)
            throws InvalidCountryException {
        try {
            return calculator.score(countryCode);
        } catch (InvalidCountryException e) {
            LOG.log(Level.INFO, "Rejected forecast request: {0}", e.getMessage());
            throw e;
        }
    }
}
