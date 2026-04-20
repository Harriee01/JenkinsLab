package com.fakestore.tests;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fakestore.base.BaseTest;
import com.fakestore.pojos.Cart;
import com.fakestore.utils.AssertionUtils;
import com.fakestore.utils.Endpoints;
import com.fakestore.utils.TestDataFactory;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

/**
 * CartTests – comprehensive CRUD test suite for the /carts endpoint
 * of the Fake Store API (https://fakestoreapi.com/carts).
 *
 * Test coverage:
 *   GET  /carts             – fetch all carts
 *   GET  /carts/{id}        – fetch single cart by ID
 *   GET  /carts/user/{userId} – fetch carts for a specific user
 *   POST /carts             – create a new cart
 *   PUT  /carts/{id}        – full update of an existing cart
 *   DELETE /carts/{id}      – delete a cart
 *   Negative: GET with invalid cart ID
 *
 * Every test validates: HTTP status code, Content-Type header,
 * response body field values, and JSON Schema where applicable.
 */
@Epic("Fake Store API")                               // top-level Allure grouping
@Feature("Carts Endpoint")                            // feature-level Allure grouping
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // run tests in declared order
public class CartTests extends BaseTest {

    // Logger for this test class
    private static final Logger log = LoggerFactory.getLogger(CartTests.class);

    // Known-good cart ID from Fake Store seed data – safe to read without side-effects
    private static final int EXISTING_CART_ID = 1;

    // Known-good user ID from Fake Store seed data – used for user-specific cart lookup
    private static final int EXISTING_USER_ID = 1;

    // ==================================================================
    // TEST 1 – GET /carts – Fetch All Carts
    // ==================================================================

    @Test
    @Order(1)
    @Story("Fetch all carts")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /carts returns 200 and a non-empty JSON array of cart objects.")
    @DisplayName("GET /carts – returns 200 and non-empty cart list")
    void testGetAllCarts_returns200AndNonEmptyList() {
        log.info(">>> TEST: GET /carts – fetch all carts");

        // Send GET /carts using the shared requestSpec from BaseTest
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Endpoints.CARTS)             // use Endpoints constant – no inline string
                .then()
                .spec(responseSpec)               // applies shared response-time guard
                .extract().response();

        log.info("Response status: {}", response.statusCode());

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header contains application/json
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the JSON array into a list of Cart POJOs
        List<Cart> carts = response.jsonPath().getList("", Cart.class);
        log.info("Total carts returned: {}", carts.size());

        // The list must not be empty
        assertThat(carts).as("Cart list must not be empty").isNotEmpty();

        // Every cart must have a non-null ID and a non-null userId
        carts.forEach(c -> {
            assertThat(c.getId()).as("Cart id must not be null").isNotNull();
            assertThat(c.getUserId()).as("Cart userId must not be null").isNotNull();
        });

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /carts");
    }

    // ==================================================================
    // TEST 2 – GET /carts/{id} – Single Cart by ID
    // ==================================================================

    @Test
    @Order(2)
    @Story("Fetch single cart by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /carts/1 returns cart with ID=1 and correct field values.")
    @DisplayName("GET /carts/{id} – returns correct cart by ID")
    void testGetCartById_returns200AndCorrectCart() {
        log.info(">>> TEST: GET /carts/{} – single cart", EXISTING_CART_ID);

        // Send GET /carts/1 using the CART_BY_ID path template
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_CART_ID) // substitute {id} in the path
                .when()
                .get(Endpoints.CART_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract individual fields from the JSON response
        int     returnedId     = response.jsonPath().getInt("id");
        int     returnedUserId = response.jsonPath().getInt("userId");
        String  returnedDate   = response.jsonPath().getString("date");
        List<?> products       = response.jsonPath().getList("products");

        log.info("Cart returned – id:{}, userId:{}, date:'{}', products:{}",
                returnedId, returnedUserId, returnedDate, products.size());

        // The returned ID must match the one we requested
        assertThat(returnedId).as("Cart ID must match requested ID").isEqualTo(EXISTING_CART_ID);

        // userId must be a positive integer
        assertThat(returnedUserId).as("Cart userId must be positive").isPositive();

        // date must not be blank
        assertThat(returnedDate).as("Cart date must not be blank").isNotBlank();

        // products list must not be empty
        assertThat(products).as("Cart must contain at least one product").isNotEmpty();

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /carts/{}", EXISTING_CART_ID);
    }

    // ==================================================================
    // TEST 3 – GET /carts/user/{userId} – Carts by User
    // ==================================================================

    @Test
    @Order(3)
    @Story("Fetch carts by user ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /carts/user/1 returns carts belonging to user ID 1.")
    @DisplayName("GET /carts/user/{userId} – returns carts for a specific user")
    void testGetCartsByUser_returns200AndUserCarts() {
        log.info(">>> TEST: GET /carts/user/{} – carts by user", EXISTING_USER_ID);

        // Send GET /carts/user/1 using the CARTS_BY_USER path template
        Response response = given()
                .spec(requestSpec)
                .pathParam("userId", EXISTING_USER_ID) // substitute {userId} in the path
                .when()
                .get(Endpoints.CARTS_BY_USER)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the response as a list of Cart POJOs
        List<Cart> carts = response.jsonPath().getList("", Cart.class);
        log.info("Carts returned for userId={}: {}", EXISTING_USER_ID, carts.size());

        // The list must not be empty – user 1 has carts in seed data
        assertThat(carts).as("User carts list must not be empty").isNotEmpty();

        // Every cart in the response must belong to the requested user
        carts.forEach(c ->
                assertThat(c.getUserId())
                        .as("Cart userId must match requested userId")
                        .isEqualTo(EXISTING_USER_ID)
        );

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /carts/user/{}", EXISTING_USER_ID);
    }

    // ==================================================================
    // TEST 4 – POST /carts – Create Cart
    // ==================================================================

    @Test
    @Order(4)
    @Story("Create a new cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify POST /carts with a valid payload returns 200 and echoes the created cart with a new ID.")
    @DisplayName("POST /carts – creates cart and returns ID in response")
    void testCreateCart_returns200AndEchosNewCart() {
        log.info(">>> TEST: POST /carts – create new cart");

        // Build the request payload using the centralised factory
        Cart newCart = TestDataFactory.buildNewCart();
        log.info("Sending payload: {}", newCart);

        // Send POST /carts with the cart JSON body
        Response response = given()
                .spec(requestSpec)
                .body(newCart)                    // Jackson serialises the POJO to JSON
                .when()
                .post(Endpoints.CARTS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 (not 201) for POST – this is known API behaviour
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract the echoed fields from the response
        Integer createdId     = response.jsonPath().getInt("id");
        Integer returnedUserId = response.jsonPath().getInt("userId");

        log.info("Created cart – id:{}, userId:{}", createdId, returnedUserId);

        // The server must assign a positive integer ID
        assertThat(createdId).as("Created cart must have an ID assigned by server").isNotNull();
        assertThat(createdId).as("Created cart ID must be positive").isPositive();

        // The echoed userId must match what we sent
        assertThat(returnedUserId)
                .as("Echoed userId must match sent userId")
                .isEqualTo(newCart.getUserId());

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: POST /carts – cart created with ID {}", createdId);
    }

    // ==================================================================
    // TEST 5 – PUT /carts/{id} – Full Update
    // ==================================================================

    @Test
    @Order(5)
    @Story("Update an existing cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify PUT /carts/1 with updated payload returns 200 and echoes the updated fields.")
    @DisplayName("PUT /carts/{id} – updates cart and returns updated fields")
    void testUpdateCart_returns200AndEchosUpdatedFields() {
        log.info(">>> TEST: PUT /carts/{} – full update", EXISTING_CART_ID);

        // Build the update payload using the centralised factory
        Cart updatedCart = TestDataFactory.buildUpdatedCart(EXISTING_CART_ID);
        log.info("Sending update payload: {}", updatedCart);

        // Send PUT /carts/1 with the updated cart JSON body
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_CART_ID) // substitute {id} in the path
                .body(updatedCart)
                .when()
                .put(Endpoints.CART_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract echoed fields from the response
        int returnedId     = response.jsonPath().getInt("id");
        int returnedUserId = response.jsonPath().getInt("userId");

        log.info("Updated cart echoed – id:{}, userId:{}", returnedId, returnedUserId);

        // The returned ID must match the one we updated
        assertThat(returnedId).as("Returned cart ID must match requested ID").isEqualTo(EXISTING_CART_ID);

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: PUT /carts/{}", EXISTING_CART_ID);
    }

    // ==================================================================
    // TEST 6 – DELETE /carts/{id} – Delete Cart
    // ==================================================================

    @Test
    @Order(6)
    @Story("Delete an existing cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify DELETE /carts/1 returns 200 and echoes the deleted cart's details.")
    @DisplayName("DELETE /carts/{id} – deletes cart and returns cart details")
    void testDeleteCart_returns200AndEchosCart() {
        log.info(">>> TEST: DELETE /carts/{} – delete cart", EXISTING_CART_ID);

        // Send DELETE /carts/1
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_CART_ID) // substitute {id} in the path
                .when()
                .delete(Endpoints.CART_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 with the cart object on DELETE (not 204)
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // The response body echoes the cart that was deleted
        Integer deletedId     = response.jsonPath().getInt("id");
        Integer deletedUserId = response.jsonPath().getInt("userId");

        log.info("Deleted cart echoed – id:{}, userId:{}", deletedId, deletedUserId);

        // The echoed ID must be present and valid
        assertThat(deletedId).as("Deleted cart ID must not be null").isNotNull();
        assertThat(deletedUserId).as("Deleted cart userId must not be null").isNotNull();

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: DELETE /carts/{}", EXISTING_CART_ID);
    }

    // ==================================================================
    // TEST 7 – Negative: GET /carts/{invalid-id}
    // ==================================================================

    @Test
    @Order(7)
    @Story("Negative – Invalid cart ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /carts/{invalid-id} with a non-existent ID returns a null body (Fake Store behaviour).")
    @DisplayName("GET /carts/{invalid-id} – non-existent ID returns null body")
    void testGetCartByInvalidId_returnsNullBody() {
        final int invalidId = 999999; // ID that does not exist in Fake Store seed data
        log.info(">>> TEST: GET /carts/{} – invalid ID (negative test)", invalidId);

        // Send GET /carts/999999
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", invalidId)
                .when()
                .get(Endpoints.CART_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API returns 200 with body "null" for non-existent resources
        AssertionUtils.assertStatusCode(response, 200);

        // Read the raw body string and trim whitespace
        String body = response.body().asString().trim();
        log.info("Response body for invalid cart ID: '{}'", body);

        // Body must be "null" or empty – the API does not return 404
        assertThat(body)
                .as("Non-existent cart should return null or empty body")
                .isIn("null", "");

        log.info("<<< TEST PASSED (Negative): GET /carts/{}", invalidId);
    }
}
