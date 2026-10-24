# Economic Forecast SOAP Web Service – Plan

## Overview

Greenfield project (workspace currently contains only `.bob/mcp.json`).

Build a code-first SOAP web service (Jakarta XML Web Services 4.0) that returns an economic forecast score (`double`, 0–100) for a given country code. It runs on WebSphere Liberty 25.x. The project uses Java 21, WAR packaging, Maven with the Maven Wrapper, and `liberty-maven-plugin` for dev mode and run.

Decisions confirmed with the user:
- **Style:** code-first, annotated `@WebService` class. The WSDL is generated at runtime. No Docker.
- **Operation:** input is a country code (`String`) only. Output is a `double` score 0–100. The score comes from a small in-memory table of hard-coded sample indicators (GDP growth, inflation, unemployment) combined by a weighted formula.
- **Build:** Java 21, WAR, `liberty-maven-plugin`, `server.xml` in `src/main/liberty/config`, JUnit test.
- **Compatibility:** compile against Jakarta EE 11 APIs (`provided` scope). In `server.xml` enable `servlet-6.1` and `xmlWS-4.0`, with no `jakartaee-11.0` umbrella feature. Verify in dev mode. If the feature resolver rejects the combination, fall back to EE 10 features (`jakartaee-10.0` / `servlet-6.0` + `xmlWS-4.0`) and EE 10 APIs.

Security-rule constraints that apply (project `security.md`):
- Use the latest stable, maintained versions of every dependency and plugin. Verify the versions at implementation time. Do not guess them.
- Bind the HTTP endpoint to `127.0.0.1` (`host` attribute), never `0.0.0.0`.
- Validate input with an allowlist (ISO 3166-1 alpha-2 codes known to the table).
- Return generic fault messages to clients and log details server-side only, with no sensitive data.
- Add a `.gitignore`.
- Use TLS 1.2+ if HTTPS is configured. Plain HTTP on localhost is acceptable for dev only. Document this.

## Target structure

```
.gitignore
.mvn/wrapper/maven-wrapper.properties
mvnw, mvnw.cmd
pom.xml
src/main/java/com/example/forecast/
    EconomicForecastService.java       (@WebService endpoint)
    ForecastCalculator.java            (weighted scoring logic + indicator table)
    EconomicIndicators.java            (record: gdpGrowth, inflation, unemployment)
    InvalidCountryException.java       (@WebFault / checked exception, optional)
src/main/liberty/config/server.xml
src/main/webapp/WEB-INF/web.xml        (only if required)
src/test/java/com/example/forecast/ForecastCalculatorTest.java
src/test/java/com/example/forecast/EconomicForecastServiceTest.java
README.md                              (build/run/test + sample SOAP request)
```

Package name and artifact IDs are placeholders and can be changed during implementation.

---

## Sub-Task 1 – Maven project skeleton and Maven Wrapper

**Intent:** Establish a buildable WAR project with the wrapper, so every later step uses `./mvnw`.

**Expected Outcomes:**
- `./mvnw -v` runs and downloads Maven.
- `./mvnw clean package` produces an (empty) WAR on Java 21.
- `.gitignore` exists.

**Todo List:**
1. Create `pom.xml` with `packaging=war`, `maven.compiler.release=21`, and UTF-8 encoding.
2. Add `jakarta.platform:jakarta.jakartaee-api` 11.x (`provided`). It does not include the XML WS API, so also add `jakarta.xml.ws:jakarta.xml.ws-api` 4.x and `jakarta.jws:jakarta.jws-api` (`provided`) if required for `@WebService`/`@WebMethod`/`@WebParam`. Verify the artifacts and latest versions.
3. Add JUnit Jupiter (latest 5.x/6.x stable) as a `test` dependency and `maven-surefire-plugin` (latest).
4. Add `maven-war-plugin` (latest) with `failOnMissingWebXml=false`.
5. Generate the Maven Wrapper. Use `mvn wrapper:wrapper -Dmaven=<latest stable 3.9.x>` if Maven is installed. Otherwise create `mvnw`, `mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties` by hand, matching the official wrapper layout. Make `mvnw` executable.
6. Add `.gitignore` (`target/`, IDE files, `.env`, Liberty `wlp/` output).

**Relevant Context:** Workspace is empty. Wrapper distribution URL must use HTTPS (Maven Central).

**Status:** `[x] done`

---

## Sub-Task 2 – Liberty configuration and plugin

**Intent:** Make the project runnable and debuggable on WebSphere Liberty via `./mvnw liberty:dev` and `liberty:run`.

**Expected Outcomes:**
- `./mvnw liberty:dev` starts Liberty with the app deployed.
- The server log shows the `servlet-6.1` and `xmlWS-4.0` features installed and loaded, or the documented EE 10 fallback is applied.

**Todo List:**
1. Add `io.openliberty.tools:liberty-maven-plugin` (latest) to the `pom.xml` `<build>`. Configure `runtimeArtifact` for WebSphere Liberty 25.x (`com.ibm.websphere.appserver.runtime`, a suitable artifactId and version). Verify the coordinates and the licence implications for Maven-distributed WebSphere Liberty.
2. Create `src/main/liberty/config/server.xml`:
   - `<featureManager>` with `servlet-6.1` and `xmlWS-4.0`.
   - `<httpEndpoint host="127.0.0.1" httpPort="9080" httpsPort="9443"/>`.
   - `<webApplication contextRoot="/forecast" location="…war"/>`, or rely on the plugin's loose-app deployment.
3. Run `./mvnw liberty:dev`. Check whether the feature resolver accepts `servlet-6.1` + `xmlWS-4.0`.
4. **Fallback:** if rejected, switch to `jakartaee-10.0`, or `servlet-6.0` + `xmlWS-4.0`. Change the compile API to Jakarta EE 10 and record the outcome in this plan file.
5. Keep any server config secrets (keystore passwords) out of `server.xml`. Use environment variables or variable substitution.

**Relevant Context:** `src/main/liberty/config/server.xml` is the standard `liberty-maven-plugin` config location. Security rule: no `0.0.0.0` binding.

**Status:** `[x] done`

---

## Sub-Task 3 – Forecast domain logic

**Intent:** Implement the scoring independent of SOAP so it can be unit tested.

**Expected Outcomes:**
- `ForecastCalculator.score(String countryCode)` returns a `double` in [0, 100].
- It throws a clear exception for unknown or invalid codes.

**Todo List:**
1. Define the `EconomicIndicators` record (gdpGrowth %, inflation %, unemployment %).
2. Define the hard-coded sample table for roughly 5–8 countries (for example US, DE, JP, GB, FR, BR, IN), clearly marked as sample data.
3. Define the weighted formula: normalise each indicator to 0–100 against fixed reference ranges, with higher GDP growth better, lower inflation better, and lower unemployment better. Use named constant weights summing to 1.0 (for example GDP 0.4, inflation 0.3, unemployment 0.3). Clamp the result to [0, 100] and round to 2 decimals.
4. Validate input: trim, upper-case, allowlist match (the table keys and a 2-letter pattern). Reject null or blank.
5. Log with structured, non-sensitive messages (`java.util.logging`, which Liberty supports natively).

**Relevant Context:** Pure Java, no Jakarta dependencies, in `ForecastCalculator.java` and `EconomicIndicators.java`.

**Status:** `[x] done`

---

## Sub-Task 4 – SOAP endpoint

**Intent:** Expose the calculator as a SOAP operation with a generated WSDL.

**Expected Outcomes:**
- `http://127.0.0.1:9080/forecast/EconomicForecastService?wsdl` returns a WSDL with a `getForecastScore(countryCode)` operation returning `double`.
- A SOAP request for a valid code returns a score.
- An invalid code returns a SOAP fault with a generic message.

**Todo List:**
1. Create `EconomicForecastService` annotated with `@WebService(serviceName=…, targetNamespace=…)`, with `@WebMethod` and `@WebParam`/`@WebResult` naming for stable WSDL names.
2. Delegate to `ForecastCalculator`. Map validation errors to a `@WebFault` or `SOAPFaultException` with a generic message and no stack trace. Log the detail server-side.
3. Confirm that Liberty auto-publishes the `@WebService` class from the WAR. Add `web.xml` or `sun-jaxws`-style config only if the runtime test shows it is needed.
4. Verify manually in dev mode: fetch the WSDL and call with `curl` using a SOAP 1.1 envelope.

**Relevant Context:** Liberty xmlWS-4.0 scans the WAR for `@WebService` classes. Endpoint URL is `<contextRoot>/<serviceName>`.

**Status:** `[x] done`

---

## Sub-Task 5 – Tests and documentation

**Intent:** Provide automated verification and run instructions.

**Expected Outcomes:**
- `./mvnw test` passes.
- README documents the commands and a sample request/response.

**Todo List:**
1. `ForecastCalculatorTest`: known country gives a score within [0, 100]; unknown, null, blank and malformed codes are rejected; case and whitespace are normalised; a better-indicator country scores higher than a worse one.
2. `EconomicForecastServiceTest`: a direct-instantiation test of the service delegating and mapping errors to the fault.
3. `README.md`: prerequisites (Java 21), `./mvnw clean package`, `./mvnw liberty:dev`, WSDL URL, sample SOAP envelope, EE 11 / EE 10 fallback note, and the localhost-only and plain-HTTP-for-dev note.
4. Final validation: `./mvnw clean verify` with no warnings or failures, and a manual SOAP call in dev mode.

**Relevant Context:** Test sources are under `src/test/java/com/example/forecast/`.

**Status:** `[x] done`

---

## Open Risks
- `servlet-6.1` + `xmlWS-4.0` compatibility on Liberty 25.x is unverified and is resolved at runtime in Sub-Task 2, with the EE 10 fallback.
- The exact `liberty-maven-plugin` `runtimeArtifact` coordinates for WebSphere Liberty need verification. Open Liberty is an alternative if they are not resolvable.
- Dependency versions must be looked up at implementation time. This plan does not pin any.

## Implementation Notes
- Verified on Liberty 26.0.0.10 (`com.ibm.websphere.appserver.runtime:wlp-kernel`): `servlet-6.1` + `xmlWS-4.0` resolve together. The EE 10 fallback was not needed.
- Versions used: Maven 3.9.16 (wrapper 3.3.4, SHA-256 pinned), liberty-maven-plugin 3.12.4, JUnit 6.1.3, jakartaee-api 11.0.0, jakarta.xml.ws-api 4.0.3, jakarta.jws-api 3.0.0.
- Added `InvalidCountryFault` bean so the WSDL fault schema is clean.
- Validation: `./mvnw clean verify` passes (14 tests). WSDL fetched and SOAP success and fault calls checked on a live server.
