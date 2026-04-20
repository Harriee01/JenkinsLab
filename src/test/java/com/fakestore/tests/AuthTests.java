package com.fakestore.tests;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fakestore.base.BaseTest;
import com.fakestore.utils.AssertionUtils;
import com.fakestore.utils.Endpoints;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

/**
 * AuthTests – test suite for the /auth/login endpoint
 * of the Fake Store API (https://fakestoreapi.com/auth/login).
 *
 * Test coverage:
 *   POST /auth/login – successful login with valid credentials
 *   POST /auth/login – failed login with invalid credentials (negative)
 *   POST /auth/login – failed login with missing fields (negative)
 *
 * Every test validates: HTTP status code, Content-Type header,
 * and response body field values.
 *
 * NOTE: Fake Store API uses seed credentials. Valid credentials are:
 *   username: "mor_2314"
 *   password: "83r5^_"
 * These are publicly documented at https://fakestoreapi.com/docs
 */
@Epic("Fake Store API")                               // top-level Allure grouping
@Feature("Auth Endpoint")                             // feature-level Allure grouping
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // run tests in declared order
public class AuthTests extends BaseTest {

    // Logger for this test class
    private static final Logger log = LoggerFactory.getLogger(AuthTests.class);

    // Valid seed credentials from Fake Store API documentation
    // These are publicly documented test credentials – not real user data
    private static final String VALID_USERNAME = "mor_2314";
    private static final String VALID_PASSWORD = "83r5^_";

    // ==================================================================
    // TEST 1 – POST /auth/login – Successful Login
    // ==================================================================

    @Test
    @Order(1)
    @Story("Successful login with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify POST /auth/login with valid credentials returns 200 and a non-blank JWT token.")
    @DisplayName("POST /auth/login – valid credentials return JWT token")
    void testLogin_withValidCredentials_returnsToken() {
        log.info(">>> TEST: POST /auth/login – valid credentials");

        // Build the login request body as a JSON string
        // Using a raw string here because the auth payload is simple (username + password only)
        String loginBody = String.format(
                "{\"username\": \"%s\", \"password\": \"%s\"}",
                VALID_USERNAME,  // seed username from Fake Store API docs
                VALID_PASSWORD   // seed password from Fake Store API docs
        );
        log.info("Sending login payload for username: {}", VALID_USERNAME);

        // Send POST /auth/login with the credentials JSON body
        Response response = given()
                .spec(requestSpec)
                .body(loginBody)                  // raw JSON string as the request body
                .when()
                .post(Endpoints.AUTH_LOGIN)       // use Endpoints constant – no inline string
                .then()
                .spec(responseSpec)               // applies shared response-time guard
                .extract().response();

        log.info("Login response status: {}", response.statusCode());

        // Assert HTTP 200 OK – successful authentication
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header contains application/json
        AssertionUtils.assertContentType(response, "application/json");

        // Extract the JWT token from the response body
        String token = response.jsonPath().getString("token");
        log.info("JWT token received (first 20 chars): {}...",
                token != null && token.length() > 20 ? token.substring(0, 20) : token);

        // The token must not be null or blank
        assertThat(token)
                .as("JWT token must not be null or blank on successful login")
                .isNotNull()
                .isNotBlank();

        // A JWT token has 3 parts separated by dots (header.payload.signature).
        // The assertThat above guarantees token is non-null, but we add an explicit check
        // to satisfy static analysis tools.
        if (token != null) {
            assertThat(token.split("\\."))
                    .as("JWT token must have 3 parts separated by dots")
                    .hasSize(3);
        }

        // Attach the response body to the Allure report (token is visible for debugging)
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: POST /auth/login – token received");
    }

    // ==================================================================
    // TEST 2 – Negative: POST /auth/login – Invalid Credentials
    // ==================================================================

    @Test
    @Order(2)
    @Story("Negative – Login with invalid credentials")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify POST /auth/login with wrong credentials returns a non-200 status or an error message.")
    @DisplayName("POST /auth/login – invalid credentials return error response")
    void testLogin_withInvalidCredentials_returnsError() {
        log.info(">>> TEST: POST /auth/login – invalid credentials (negative test)");

        // Build a login body with deliberately wrong credentials
        String invalidLoginBody = "{\"username\": \"wronguser\", \"password\": \"wrongpassword\"}";
        log.info("Sending invalid login payload");

        // Send POST /auth/login with invalid credentials
        Response response = given()
                .spec(requestSpec)
                .body(invalidLoginBody)           // wrong username and password
                .when()
                .post(Endpoints.AUTH_LOGIN)
                .then()
                // Do NOT apply responseSpec here – we expect a non-200 status
                .extract().response();

        log.info("Invalid login response status: {}", response.statusCode());
        log.info("Invalid login response body: {}", response.body().asString());

        // Fake Store API returns 401 for invalid credentials
        // Assert the status code is NOT 200 (authentication must fail)
        assertThat(response.statusCode())
                .as("Invalid credentials must not return HTTP 200")
                .isNotEqualTo(200);

        // Attach the response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED (Negative): POST /auth/login – invalid credentials rejected");
    }

    // ==================================================================
    // TEST 3 – Negative: POST /auth/login – Missing Fields
    // ==================================================================

    @Test
    @Order(3)
    @Story("Negative – Login with missing fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify POST /auth/login with an empty body returns a non-200 status or an error message.")
    @DisplayName("POST /auth/login – missing fields return error response")
    void testLogin_withMissingFields_returnsError() {
        log.info(">>> TEST: POST /auth/login – missing fields (negative test)");

        // Send POST /auth/login with an empty JSON object (no username or password)
        Response response = given()
                .spec(requestSpec)
                .body("{}")                        // empty JSON – no credentials provided
                .when()
                .post(Endpoints.AUTH_LOGIN)
                .then()
                // Do NOT apply responseSpec here – we expect a non-200 status
                .extract().response();

        log.info("Missing fields login response status: {}", response.statusCode());
        log.info("Missing fields login response body: {}", response.body().asString());

        // Authentication must fail when credentials are missing
        assertThat(response.statusCode())
                .as("Missing credentials must not return HTTP 200")
                .isNotEqualTo(200);

        // Attach the response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED (Negative): POST /auth/login – missing fields rejected");
    }
}
