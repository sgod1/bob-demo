package com.example.forecast;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Computes an economic forecast score (0-100) from hard-coded SAMPLE indicators.
 * The data is illustrative only and does not represent real economic figures.
 */
public class ForecastCalculator {

    static final double WEIGHT_GDP = 0.4;
    static final double WEIGHT_INFLATION = 0.3;
    static final double WEIGHT_UNEMPLOYMENT = 0.3;

    // Reference ranges used to normalise each indicator to 0..100.
    private static final double GDP_MIN = -2.0;
    private static final double GDP_MAX = 8.0;
    private static final double INFLATION_BEST = 2.0;
    private static final double INFLATION_WORST = 12.0;
    private static final double UNEMPLOYMENT_BEST = 3.0;
    private static final double UNEMPLOYMENT_WORST = 15.0;

    private static final Pattern COUNTRY_CODE = Pattern.compile("[A-Z]{2}");

    private static final Map<String, EconomicIndicators> SAMPLE_DATA = Map.of(
            "US", new EconomicIndicators(2.5, 3.1, 4.0),
            "DE", new EconomicIndicators(0.3, 2.4, 3.2),
            "JP", new EconomicIndicators(1.0, 2.8, 2.6),
            "GB", new EconomicIndicators(1.1, 3.4, 4.3),
            "FR", new EconomicIndicators(0.9, 2.3, 7.3),
            "BR", new EconomicIndicators(2.9, 4.5, 7.8),
            "IN", new EconomicIndicators(6.5, 4.9, 7.6),
            "AR", new EconomicIndicators(-1.5, 11.0, 7.0));

    /**
     * @param countryCode ISO 3166-1 alpha-2 code (case-insensitive, surrounding whitespace ignored)
     * @return score in the range 0-100, rounded to two decimals
     * @throws InvalidCountryException if the code is null, malformed or unknown
     */
    public double score(String countryCode) throws InvalidCountryException {
        EconomicIndicators indicators = SAMPLE_DATA.get(normalize(countryCode));
        if (indicators == null) {
            throw new InvalidCountryException("Unsupported country code");
        }

        double gdp = scale(indicators.gdpGrowth(), GDP_MIN, GDP_MAX);
        double inflation = scale(indicators.inflation(), INFLATION_WORST, INFLATION_BEST);
        double unemployment = scale(indicators.unemployment(), UNEMPLOYMENT_WORST, UNEMPLOYMENT_BEST);

        double weighted = WEIGHT_GDP * gdp + WEIGHT_INFLATION * inflation + WEIGHT_UNEMPLOYMENT * unemployment;
        double clamped = Math.max(0.0, Math.min(100.0, weighted));
        return Math.round(clamped * 100.0) / 100.0;
    }

    private static String normalize(String countryCode) throws InvalidCountryException {
        if (countryCode == null) {
            throw new InvalidCountryException("Country code is required");
        }
        String normalized = countryCode.strip().toUpperCase(Locale.ROOT);
        if (!COUNTRY_CODE.matcher(normalized).matches()) {
            throw new InvalidCountryException("Invalid country code format");
        }
        return normalized;
    }

    /** Linearly maps value from [worst, best] to [0, 100], clamped. */
    private static double scale(double value, double worst, double best) {
        double ratio = (value - worst) / (best - worst);
        return Math.max(0.0, Math.min(1.0, ratio)) * 100.0;
    }
}
