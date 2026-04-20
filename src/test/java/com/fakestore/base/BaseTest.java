package com.fakestore.base;

import static org.hamcrest.Matchers.lessThan;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fakestore.config.AppConfig;
import com.fakestore.utils.AllureEnvironmentWriter;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * BaseTest – abstract parent class for all test classes in this framework.
 *
 * Responsibilities:
 *   - Bootstrap the shared RequestSpecification once per JVM run.
 *   - Configure a shared ResponseSpecification with a response-time guard.
 *   - Attach the AllureRestAssured filter so every HTTP exchange is captured
 *     in the Allure report automatically.
 *   - Wire in connection and socket timeouts from AppConfig.
 *   - Write the Allure environment.properties file for the report overview page.
 *
 * Child classes inherit requestSpec and responseSpec as protected static fields
 * and use them directly in their test methods via given().spec(requestSpec).
 */
public abstract class BaseTest {

    // Logger for the base class – child classes define their own loggers
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    /**
     * Shared RequestSpecification – reused across all tests to avoid repeating
     * base URL, content type, timeouts, and filters in every test method.
     * Declared protected so all subclasses can access it directly.
     */
    protected static RequestSpecification requestSpec;

    /**
     * Shared ResponseSpecification – applies a 15-second response-time guard
     * to every test. Individual tests can layer additional matchers on top.
     * Declared protected so all subclasses can access it directly.
     */
    protected static ResponseSpecification responseSpec;

    /**
     * One-time setup method executed before any test in the suite runs.
     * @BeforeAll ensures this runs exactly once per JVM, not before every test.
     * The method is static because @BeforeAll requires static context in JUnit 5.
     */
    @BeforeAll
    static void setUpRestAssured() {
        // Read the base URL from AppConfig (env var → system property → default)
        String baseUrl = AppConfig.getBaseUrl();

        // Read timeout values from AppConfig (env var → system property → default)
        int connectionTimeout = AppConfig.getConnectionTimeoutMs();
        int socketTimeout     = AppConfig.getSocketTimeoutMs();

        // Log the resolved configuration values for CI debugging
        log.info("========================================");
        log.info("Initialising REST Assured base config");
        log.info("Base URL           : {}", baseUrl);
        log.info("Connection timeout : {} ms", connectionTimeout);
        log.info("Socket timeout     : {} ms", socketTimeout);
        log.info("Java               : {} {}", System.getProperty("java.vendor"), System.getProperty("java.version"));
        log.info("OS                 : {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("========================================");

        // Resolve the Allure results directory from the system property set by Surefire,
        // falling back to "target/allure-results" if the property is not set
        String allureResultsDir = System.getProperty(
                "allure.results.directory", "target/allure-results"
        );

        // Write the environment.properties file so the Allure report overview page
        // shows the base URL, Java version, framework versions, and OS details
        AllureEnvironmentWriter.write(allureResultsDir);

        // ----------------------------------------------------------------
        // Build the shared RequestSpecification
        // ----------------------------------------------------------------
        requestSpec = new RequestSpecBuilder()
                // Set the base URI from AppConfig – no hard-coded URL anywhere
                .setBaseUri(baseUrl)
                // All Fake Store API endpoints produce and consume JSON
                .setContentType(ContentType.JSON)
                // Tell the server we accept JSON responses
                .setAccept(ContentType.JSON)
                // Allure filter – auto-attaches every HTTP request and response
                // (headers, body, status) to the current Allure test step
                .addFilter(new AllureRestAssured())
                // Log full request details (method, URI, headers, body) to console/file
                // This is invaluable for debugging failures in CI pipelines
                .log(LogDetail.ALL)
                // Wire in the Apache HttpClient timeout configuration
                .setConfig(
                        RestAssured.config()
                                // HttpClientConfig sets the actual Apache HttpClient parameters
                                .httpClient(
                                        HttpClientConfig.httpClientConfig()
                                                // Max milliseconds to wait while establishing a TCP connection
                                                .setParam("http.connection.timeout", connectionTimeout)
                                                // Max milliseconds to wait for data after the connection is open
                                                .setParam("http.socket.timeout", socketTimeout)
                                )
                                // Close idle connections after each response to avoid stale socket issues
                                .connectionConfig(
                                        io.restassured.config.ConnectionConfig.connectionConfig()
                                                .closeIdleConnectionsAfterEachResponse()
                                )
                )
                .build(); // produce the immutable RequestSpecification object

        // ----------------------------------------------------------------
        // Build the shared ResponseSpecification
        // ----------------------------------------------------------------
        // 15 000 ms upper-bound guards against hung connections in CI.
        // Individual tests can add more specific matchers (status code, body, etc.)
        // on top of this shared spec.
        responseSpec = new ResponseSpecBuilder()
                .expectResponseTime(lessThan(15_000L)) // fail if response takes > 15 seconds
                .build(); // produce the immutable ResponseSpecification object

        log.info("REST Assured configuration complete. Ready to run tests.");
    }
}
