package com.example.forecast;

/**
 * Sample macro-economic indicators for a country (percentages).
 *
 * @param gdpGrowth    annual real GDP growth in percent
 * @param inflation    annual consumer price inflation in percent
 * @param unemployment unemployment rate in percent
 */
public record EconomicIndicators(double gdpGrowth, double inflation, double unemployment) {
}
