# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Commands
- Always use the wrapper `./mvnw` (Maven is not installed; pinned 3.9.16 with SHA-256 in `.mvn/wrapper/maven-wrapper.properties`).
- Full build + tests: `./mvnw -B clean verify`
- Single test class: `./mvnw -B test -Dtest=ForecastCalculatorTest`; single method: `-Dtest=ForecastCalculatorTest#inputIsTrimmedAndCaseInsensitive`
- Run on Liberty: `./mvnw liberty:dev` (hot reload) or `./mvnw liberty:run`. Server `forecastServer` is created under `target/liberty/` (WebSphere Liberty `wlp-kernel` fetched from Maven Central, not a local install).
- No linter/formatter is configured. Compiles with `--release 21` (local JDK may be newer).
- Manual check: `http://127.0.0.1:9080/forecast/EconomicForecastService?wsdl` (context root `/forecast` is set in `src/main/liberty/config/server.xml`, not web.xml; there is no web.xml).

## Non-obvious constraints
- Jakarta EE 11 removed XML Web Services from the platform API: `jakarta.xml.ws-api` and `jakarta.jws-api` are separate `provided` deps in `pom.xml`. Do not add the `jakartaee-11.0` umbrella feature; `server.xml` deliberately lists only `servlet-6.1` + `xmlWS-4.0`. If feature resolution ever fails, fall back to EE 10 (`jakartaee-10.0`) and EE 10 APIs.
- `server.xml` WAR location (`economic-forecast-service.war`) must match `<finalName>` in `pom.xml`.
- Endpoint is code-first: the WSDL is generated at runtime from `@WebService` annotations in `EconomicForecastService`. Keep `targetNamespace` (`http://forecast.example.com/`) and `@WebParam`/`@WebResult` names stable, since they define the public contract.
- Faults: `InvalidCountryException` (`@WebFault`) must expose `getFaultInfo()` returning the separate `InvalidCountryFault` bean. Without it the generated WSDL has a duplicated `message` element.
- Scoring lives in `ForecastCalculator` (pure Java, no Jakarta deps). Indicator data is hard-coded SAMPLE data; weights are constants that must sum to 1.0.

## Code style and security conventions
- Package `com.example.forecast`; 4-space indent; Javadoc on public API; records for data (`EconomicIndicators`).
- Input validation uses an allowlist (trim, upper-case with `Locale.ROOT`, `[A-Z]{2}`, then table lookup).
- Fault messages sent to clients must be generic; log detail server-side only with `java.util.logging` (parameterized, no sensitive data, no stack traces to clients).
- HTTP endpoint must stay bound to `127.0.0.1` (never `0.0.0.0`). Keep dependency and plugin versions at the latest stable release.
- Tests: JUnit Jupiter (6.x) in `src/test/java/com/example/forecast/`; service tests instantiate classes directly (no container).
