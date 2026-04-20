package com.fakestore.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * AssertionUtils – reusable, Allure-annotated assertion helpers.
 *
 * <p>Each method is annotated with {@code @Step} so that assertion steps
 * appear as named children in the Allure report timeline, giving reviewers
 * clear visibility into what was validated without reading raw code.
 *
 * <p>Every method also logs at the appropriate SLF4J level for console/CI output.
 */
public final class AssertionUtils {

    private static final Logger log = LoggerFactory.getLogger(AssertionUtils.class);

    // Utility class – no instantiation
    private AssertionUtils() {}

    // ----------------------------------------------------------------
    // Status code assertions
    // ----------------------------------------------------------------

    /**
     * Asserts that the HTTP response status code matches the expected value.
     *
     * @param response      the REST Assured {@link Response}
     * @param expectedCode  the expected HTTP status code (e.g. 200, 201)
     */
    @Step("Assert HTTP status code is {expectedCode}")
    public static void assertStatusCode(Response response, int expectedCode) {
        int actual = response.statusCode();
        log.info("Asserting status code – expected: {}, actual: {}", expectedCode, actual);

        if (actual != expectedCode) {
            log.error("Status code mismatch! Expected {} but got {}. Body: {}",
                    expectedCode, actual, response.body().asPrettyString());
        }

        assertThat("HTTP status code mismatch", actual,
                org.hamcrest.Matchers.equalTo(expectedCode));
    }

    // ----------------------------------------------------------------
    // Content-Type header assertions
    // ----------------------------------------------------------------

    /**
     * Asserts that the {@code Content-Type} response header contains the given value.
     *
     * @param response     the REST Assured {@link Response}
     * @param contentType  expected sub-string in the Content-Type header
     *                     (e.g. {@code "application/json"})
     */
    @Step("Assert Content-Type header contains '{contentType}'")
    public static void assertContentType(Response response, String contentType) {
        String actual = response.contentType();
        log.info("Asserting Content-Type – expected to contain: '{}', actual: '{}'", contentType, actual);

        assertThat("Content-Type header mismatch",
                actual, org.hamcrest.Matchers.containsString(contentType));
    }

    // ----------------------------------------------------------------
    // JSON Schema validation
    // ----------------------------------------------------------------

    /**
     * Validates the response body against a JSON Schema file located on the
     * test classpath (under {@code src/test/resources/schemas/}).
     *
     * @param response         the REST Assured {@link Response}
     * @param schemaClasspath  classpath-relative path, e.g. {@code "schemas/product.json"}
     */
    @Step("Validate JSON Schema: {schemaClasspath}")
    public static void assertJsonSchema(Response response, String schemaClasspath) {
        log.info("Validating response against JSON Schema: {}", schemaClasspath);

        // Attach schema name to the Allure step for report readability
        Allure.addAttachment("JSON Schema", "text/plain", schemaClasspath, ".txt");

        // REST Assured's built-in JSON Schema Validator integration
        response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaClasspath));

        log.info("JSON Schema validation PASSED for: {}", schemaClasspath);
    }

    // ----------------------------------------------------------------
    // Response body attachment
    // ----------------------------------------------------------------

    /**
     * Attaches the full response body to the current Allure step as a JSON attachment.
     *
     * @param response the REST Assured {@link Response}
     */
    @Step("Attach response body to Allure report")
    public static void attachResponseBody(Response response) {
        String body = response.body().asPrettyString();
        log.debug("Response body:\n{}", body);
        Allure.addAttachment("Response Body", "application/json", body, ".json");
    }
}
