# Economic Forecast SOAP Service

Code-first Jakarta XML Web Services 4.0 (SOAP) service on WebSphere Liberty. Given an ISO 3166-1
alpha-2 country code it returns an economic forecast score (`double`, 0-100).

The score is a weighted combination of **hard-coded sample** indicators (not real data):
GDP growth (0.4), inflation (0.3) and unemployment (0.3), each normalised to 0-100.
Sample countries: US, DE, JP, GB, FR, BR, IN, AR.

## Prerequisites
- Java 21 or newer (compiled with `--release 21`)
- No local Maven needed; use the wrapper (`./mvnw`, Maven 3.9.16)

## Build and test
```
./mvnw clean verify
```

## Run on Liberty
```
./mvnw liberty:dev     # dev mode with hot reload
./mvnw liberty:run     # plain run
```
Liberty (`wlp-kernel` 26.0.0.10 from Maven Central) is downloaded into `target/liberty`.
Config: [`src/main/liberty/config/server.xml`](src/main/liberty/config/server.xml) enables `servlet-6.1` and `xmlWS-4.0`.
The server binds to `127.0.0.1:9080` (plain HTTP, local development only; use TLS 1.2+ if exposed).

## Use
WSDL: <http://127.0.0.1:9080/forecast/EconomicForecastService?wsdl>

```
curl -s -H 'Content-Type: text/xml; charset=utf-8' -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:f="http://forecast.example.com/">
        <soapenv:Body><f:getForecastScore><countryCode>JP</countryCode></f:getForecastScore></soapenv:Body>
      </soapenv:Envelope>' \
  http://127.0.0.1:9080/forecast/EconomicForecastService
```
Response: `<score>69.6</score>`. An invalid or unknown code returns a SOAP fault `InvalidCountryFault`.

## Notes
- Jakarta EE 11 APIs are used at compile time (`provided`). XML Web Services was removed from the EE 11 platform,
  so `jakarta.xml.ws-api` and `jakarta.jws-api` are added separately. At runtime `servlet-6.1` + `xmlWS-4.0` load
  together on Liberty 26.0.0.10 (verified); the `jakartaee-11.0` umbrella feature is not used.
