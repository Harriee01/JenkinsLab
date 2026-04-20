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
import com.fakestore.pojos.User;
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
 * UserTests – comprehensive CRUD test suite for the /users endpoint
 * of the Fake Store API (https://fakestoreapi.com/users).
 *
 * Test coverage:
 *   GET  /users             – fetch all users
 *   GET  /users/{id}        – fetch single user by ID
 *   POST /users             – create a new user
 *   PUT  /users/{id}        – full update of an existing user
 *   DELETE /users/{id}      – delete a user
 *   Negative: GET with invalid user ID
 *
 * Every test validates: HTTP status code, Content-Type header,
 * response body field values, and JSON Schema where applicable.
 */
@Epic("Fake Store API")                               // top-level Allure grouping
@Feature("Users Endpoint")                            // feature-level Allure grouping
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // run tests in declared order
public class UserTests extends BaseTest {

    // Logger for this test class
    private static final Logger log = LoggerFactory.getLogger(UserTests.class);

    // Known-good user ID from Fake Store seed data – safe to read without side-effects
    private static final int EXISTING_USER_ID = 1;

    // ==================================================================
    // TEST 1 – GET /users – Fetch All Users
    // ==================================================================

    @Test
    @Order(1)
    @Story("Fetch all users")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /users returns 200 and a non-empty JSON array of user objects.")
    @DisplayName("GET /users – returns 200 and non-empty user list")
    void testGetAllUsers_returns200AndNonEmptyList() {
        log.info(">>> TEST: GET /users – fetch all users");

        // Send GET /users using the shared requestSpec from BaseTest
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Endpoints.USERS)             // use Endpoints constant – no inline string
                .then()
                .spec(responseSpec)               // applies shared response-time guard
                .extract().response();

        log.info("Response status: {}", response.statusCode());

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header contains application/json
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the JSON array into a list of User POJOs
        List<User> users = response.jsonPath().getList("", User.class);
        log.info("Total users returned: {}", users.size());

        // The list must not be empty
        assertThat(users).as("User list must not be empty").isNotEmpty();

        // Every user must have a non-null ID, non-blank username, and non-blank email
        users.forEach(u -> {
            assertThat(u.getId()).as("User id must not be null").isNotNull();
            assertThat(u.getUsername()).as("User username must not be blank").isNotBlank();
            assertThat(u.getEmail()).as("User email must not be blank").isNotBlank();
        });

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /users");
    }

    // ==================================================================
    // TEST 2 – GET /users/{id} – Single User by ID
    // ==================================================================

    @Test
    @Order(2)
    @Story("Fetch single user by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /users/1 returns user with ID=1 and correct field values.")
    @DisplayName("GET /users/{id} – returns correct user by ID")
    void testGetUserById_returns200AndCorrectUser() {
        log.info(">>> TEST: GET /users/{} – single user", EXISTING_USER_ID);

        // Send GET /users/1 using the USER_BY_ID path template
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_USER_ID) // substitute {id} in the path
                .when()
                .get(Endpoints.USER_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract individual fields from the JSON response
        int    returnedId       = response.jsonPath().getInt("id");
        String returnedEmail    = response.jsonPath().getString("email");
        String returnedUsername = response.jsonPath().getString("username");
        String returnedPhone    = response.jsonPath().getString("phone");

        log.info("User returned – id:{}, email:'{}', username:'{}', phone:'{}'",
                returnedId, returnedEmail, returnedUsername, returnedPhone);

        // The returned ID must match the one we requested
        assertThat(returnedId).as("User ID must match requested ID").isEqualTo(EXISTING_USER_ID);

        // email must not be blank
        assertThat(returnedEmail).as("User email must not be blank").isNotBlank();

        // username must not be blank
        assertThat(returnedUsername).as("User username must not be blank").isNotBlank();

        // Validate the nested name object
        String firstname = response.jsonPath().getString("name.firstname");
        String lastname  = response.jsonPath().getString("name.lastname");
        assertThat(firstname).as("User firstname must not be blank").isNotBlank();
        assertThat(lastname).as("User lastname must not be blank").isNotBlank();

        // Validate the nested address object
        String city    = response.jsonPath().getString("address.city");
        String street  = response.jsonPath().getString("address.street");
        String zipcode = response.jsonPath().getString("address.zipcode");
        assertThat(city).as("User address city must not be blank").isNotBlank();
        assertThat(street).as("User address street must not be blank").isNotBlank();
        assertThat(zipcode).as("User address zipcode must not be blank").isNotBlank();

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /users/{}", EXISTING_USER_ID);
    }

    // ==================================================================
    // TEST 3 – POST /users – Create User
    // ==================================================================

    @Test
    @Order(3)
    @Story("Create a new user")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify POST /users with a valid payload returns 200 and echoes the created user with a new ID.")
    @DisplayName("POST /users – creates user and returns ID in response")
    void testCreateUser_returns200AndEchosNewUser() {
        log.info(">>> TEST: POST /users – create new user");

        // Build the request payload using the centralised factory
        User newUser = TestDataFactory.buildNewUser();
        log.info("Sending payload: {}", newUser);

        // Send POST /users with the user JSON body
        Response response = given()
                .spec(requestSpec)
                .body(newUser)                    // Jackson serialises the POJO to JSON
                .when()
                .post(Endpoints.USERS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 (not 201) for POST – this is known API behaviour
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract the echoed fields from the response
        Integer createdId        = response.jsonPath().getInt("id");
        String returnedEmail     = response.jsonPath().getString("email");
        String returnedUsername  = response.jsonPath().getString("username");

        log.info("Created user – id:{}, email:'{}', username:'{}'",
                createdId, returnedEmail, returnedUsername);

        // The server must assign a positive integer ID
        assertThat(createdId).as("Created user must have an ID assigned by server").isNotNull();
        assertThat(createdId).as("Created user ID must be positive").isPositive();

        // The echoed email must match what we sent
        assertThat(returnedEmail)
                .as("Echoed email must match sent email")
                .isEqualTo(newUser.getEmail());

        // The echoed username must match what we sent
        assertThat(returnedUsername)
                .as("Echoed username must match sent username")
                .isEqualTo(newUser.getUsername());

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: POST /users – user created with ID {}", createdId);
    }

    // ==================================================================
    // TEST 4 – PUT /users/{id} – Full Update
    // ==================================================================

    @Test
    @Order(4)
    @Story("Update an existing user")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify PUT /users/1 with updated payload returns 200 and echoes the updated fields.")
    @DisplayName("PUT /users/{id} – updates user and returns updated fields")
    void testUpdateUser_returns200AndEchosUpdatedFields() {
        log.info(">>> TEST: PUT /users/{} – full update", EXISTING_USER_ID);

        // Build the update payload using the centralised factory
        User updatedUser = TestDataFactory.buildUpdatedUser(EXISTING_USER_ID);
        log.info("Sending update payload: {}", updatedUser);

        // Send PUT /users/1 with the updated user JSON body
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_USER_ID) // substitute {id} in the path
                .body(updatedUser)
                .when()
                .put(Endpoints.USER_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract echoed fields from the response
        int    returnedId       = response.jsonPath().getInt("id");
        String returnedEmail    = response.jsonPath().getString("email");
        String returnedUsername = response.jsonPath().getString("username");

        log.info("Updated user echoed – id:{}, email:'{}', username:'{}'",
                returnedId, returnedEmail, returnedUsername);

        // The returned ID must match the one we updated
        assertThat(returnedId).as("Returned user ID must match requested ID").isEqualTo(EXISTING_USER_ID);

        // The echoed email must reflect the update we sent
        assertThat(returnedEmail)
                .as("Email should reflect the update")
                .isEqualTo(updatedUser.getEmail());

        // The echoed username must reflect the update we sent
        assertThat(returnedUsername)
                .as("Username should reflect the update")
                .isEqualTo(updatedUser.getUsername());

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: PUT /users/{}", EXISTING_USER_ID);
    }

    // ==================================================================
    // TEST 5 – DELETE /users/{id} – Delete User
    // ==================================================================

    @Test
    @Order(5)
    @Story("Delete an existing user")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify DELETE /users/1 returns 200 and echoes the deleted user's details.")
    @DisplayName("DELETE /users/{id} – deletes user and returns user details")
    void testDeleteUser_returns200AndEchosUser() {
        log.info(">>> TEST: DELETE /users/{} – delete user", EXISTING_USER_ID);

        // Send DELETE /users/1
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_USER_ID) // substitute {id} in the path
                .when()
                .delete(Endpoints.USER_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 with the user object on DELETE (not 204)
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // The response body echoes the user that was deleted
        Integer deletedId       = response.jsonPath().getInt("id");
        String  deletedUsername = response.jsonPath().getString("username");

        log.info("Deleted user echoed – id:{}, username:'{}'", deletedId, deletedUsername);

        // The echoed ID and username must be present and valid
        assertThat(deletedId).as("Deleted user ID must not be null").isNotNull();
        assertThat(deletedUsername).as("Deleted user username must not be blank").isNotBlank();

        // Attach the raw response body to the Allure report
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: DELETE /users/{}", EXISTING_USER_ID);
    }

    // ==================================================================
    // TEST 6 – Negative: GET /users/{invalid-id}
    // ==================================================================

    @Test
    @Order(6)
    @Story("Negative – Invalid user ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /users/{invalid-id} with a non-existent ID returns a null body (Fake Store behaviour).")
    @DisplayName("GET /users/{invalid-id} – non-existent ID returns null body")
    void testGetUserByInvalidId_returnsNullBody() {
        final int invalidId = 999999; // ID that does not exist in Fake Store seed data
        log.info(">>> TEST: GET /users/{} – invalid ID (negative test)", invalidId);

        // Send GET /users/999999
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", invalidId)
                .when()
                .get(Endpoints.USER_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API returns 200 with body "null" for non-existent resources
        AssertionUtils.assertStatusCode(response, 200);

        // Read the raw body string and trim whitespace
        String body = response.body().asString().trim();
        log.info("Response body for invalid user ID: '{}'", body);

        // Body must be "null" or empty – the API does not return 404
        assertThat(body)
                .as("Non-existent user should return null or empty body")
                .isIn("null", "");

        log.info("<<< TEST PASSED (Negative): GET /users/{}", invalidId);
    }
}
