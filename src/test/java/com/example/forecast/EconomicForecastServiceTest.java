package com.example.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EconomicForecastServiceTest {

    private final EconomicForecastService service = new EconomicForecastService();

    @Test
    void delegatesToCalculator() throws Exception {
        assertEquals(new ForecastCalculator().score("DE"), service.getForecastScore("DE"));
    }

    @Test
    void invalidCountryRaisesFault() {
        assertThrows(InvalidCountryException.class, () -> service.getForecastScore("XX"));
    }
}
