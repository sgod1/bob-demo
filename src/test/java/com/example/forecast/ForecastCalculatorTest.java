package com.example.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ForecastCalculatorTest {

    private final ForecastCalculator calculator = new ForecastCalculator();

    @Test
    void scoreIsWithinRangeForAllSampleCountries() throws Exception {
        for (String code : new String[] {"US", "DE", "JP", "GB", "FR", "BR", "IN", "AR"}) {
            double score = calculator.score(code);
            assertTrue(score >= 0.0 && score <= 100.0, code + " score out of range: " + score);
        }
    }

    @Test
    void knownScoreIsComputedWithWeightedFormula() throws Exception {
        // JP: gdp 1.0 -> 30, inflation 2.8 -> 92, unemployment 2.6 -> 100
        // 0.4*30 + 0.3*92 + 0.3*100 = 12 + 27.6 + 30 = 69.6
        assertEquals(69.6, calculator.score("JP"), 0.001);
    }

    @Test
    void inputIsTrimmedAndCaseInsensitive() throws Exception {
        assertEquals(calculator.score("US"), calculator.score("  us "));
    }

    @Test
    void healthierEconomyScoresHigherThanTroubledOne() throws Exception {
        assertTrue(calculator.score("US") > calculator.score("AR"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  ", "USA", "U", "1A", "ZZ", "US; DROP TABLE"})
    void invalidOrUnknownCodesAreRejected(String code) {
        assertThrows(InvalidCountryException.class, () -> calculator.score(code));
    }
}
