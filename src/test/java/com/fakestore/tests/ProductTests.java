package com.fakestore.tests;

// Standard Java imports
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
import com.fakestore.pojos.Product;
import com.fakestore.utils.AssertionUtils;       // centralised endpoint path constants
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
 * ProductTests – comprehensive CRUD test suite for the /products endpoint.
 *
 * Covers: GET all, GET with limit, GET by ID, GET categories,
 *         GET by category, POST create, PUT update, DELETE,
 *         negative (invalid ID), and sort order.
 *
 * Every test validates: HTTP status code, Content-Type header,
 * response body field values, and JSON Schema where applicable.
 */
@Epic("Fake Store API")                          // top-level Allure grouping
@Feature("Products Endpoint")                    // feature-level Allure grouping
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // run tests in declared order
public class ProductTests extends BaseTest {

    // Logger for this test class
    private static final Logger log = LoggerFactory.getLogger(ProductTests.class);

    // Known-good product ID from Fake Store seed data – safe to read without side-effects
    private static final int EXISTING_PRODUCT_ID = 1;

    // ==================================================================
    // TEST 1 – GET /products – Fetch All Products
    // ==================================================================

    @Test
    @Order(1)
    @Story("Fetch all products")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /products returns 200, a non-empty JSON array, and matches the JSON Schema.")
    @DisplayName("GET /products – returns 200 and non-empty product list")
    void testGetAllProducts_returns200AndNonEmptyList() {
        log.info(">>> TEST: GET /products – fetch all products");

        // Send GET request using the shared requestSpec from BaseTest
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Endpoints.PRODUCTS)          // use constant instead of inline string
                .then()
                .spec(responseSpec)               // applies shared response-time guard
                .extract().response();

        log.info("Response status: {}", response.statusCode());

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header contains application/json
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the JSON array into a list of Product POJOs
        List<Product> products = response.jsonPath().getList("", Product.class);
        log.info("Total products returned: {}", products.size());

        // The list must not be empty
        assertThat(products).as("Product list must not be empty").isNotEmpty();

        // Every product must have a non-null ID, non-blank title, and positive price
        products.forEach(p -> {
            assertThat(p.getId()).as("Product id must not be null").isNotNull();
            assertThat(p.getTitle()).as("Product title must not be blank").isNotBlank();
            assertThat(p.getPrice()).as("Product price must be positive").isPositive();
        });

        // Validate the full response body against the JSON Schema
        AssertionUtils.assertJsonSchema(response, "schemas/products-list-schema.json");

        // Attach the raw response body to the Allure report for visibility
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /products");
    }

    // ==================================================================
    // TEST 2 – GET /products?limit=N – Limited product list
    // ==================================================================

    @Test
    @Order(2)
    @Story("Fetch limited product list")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the 'limit' query parameter restricts the number of products returned.")
    @DisplayName("GET /products?limit=5 – returns exactly 5 products")
    void testGetAllProducts_withLimitParam_returnsCorrectCount() {
        log.info(">>> TEST: GET /products?limit=5 – limited product list");

        final int limit = 5; // number of products to request

        // Send GET with ?limit=5 query parameter
        Response response = given()
                .spec(requestSpec)
                .queryParam("limit", limit)       // attach query param to the request
                .when()
                .get(Endpoints.PRODUCTS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise and check the list has exactly 'limit' items
        List<Product> products = response.jsonPath().getList("", Product.class);
        log.info("Products returned with limit={}: {}", limit, products.size());

        assertThat(products)
                .as("Expected exactly %d products with ?limit=%d", limit, limit)
                .hasSize(limit);

        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED: GET /products?limit=5");
    }

    // ==================================================================
    // TEST 3 – GET /products/{id} – Single Product
    // ==================================================================

    @Test
    @Order(3)
    @Story("Fetch single product by ID")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify GET /products/1 returns product with ID=1 and correct field values.")
    @DisplayName("GET /products/{id} – returns correct product by ID")
    void testGetProductById_returns200AndCorrectProduct() {
        log.info(">>> TEST: GET /products/{} – single product", EXISTING_PRODUCT_ID);

        // Send GET /products/1 using the PRODUCT_BY_ID path template
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_PRODUCT_ID) // substitute {id} in the path
                .when()
                .get(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract individual fields from the JSON response
        int    returnedId = response.jsonPath().getInt("id");
        String title      = response.jsonPath().getString("title");
        Double price      = response.jsonPath().getDouble("price");
        String category   = response.jsonPath().getString("category");

        log.info("Product returned – id:{}, title:'{}', price:{}, category:'{}'",
                returnedId, title, price, category);

        // The returned ID must match the one we requested
        assertThat(returnedId).as("Product ID must match requested ID").isEqualTo(EXISTING_PRODUCT_ID);
        assertThat(title).as("Title must not be blank").isNotBlank();
        assertThat(price).as("Price must be positive").isPositive();
        assertThat(category).as("Category must not be blank").isNotBlank();

        // Validate the nested rating object
        Double  rate  = response.jsonPath().getDouble("rating.rate");
        Integer count = response.jsonPath().getInt("rating.count");
        assertThat(rate).as("Rating rate must be between 0 and 5").isBetween(0.0, 5.0);
        assertThat(count).as("Rating count must be non-negative").isGreaterThanOrEqualTo(0);

        // Validate against the single-product JSON Schema
        AssertionUtils.assertJsonSchema(response, "schemas/product-single-schema.json");
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: GET /products/{}", EXISTING_PRODUCT_ID);
    }

    // ==================================================================
    // TEST 4 – GET /products/categories – All Categories
    // ==================================================================

    @Test
    @Order(4)
    @Story("Fetch all product categories")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /products/categories returns a non-empty array of the 4 known category strings.")
    @DisplayName("GET /products/categories – returns all categories")
    void testGetCategories_returns200AndKnownCategories() {
        log.info(">>> TEST: GET /products/categories");

        // Send GET /products/categories
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Endpoints.PRODUCT_CATEGORIES)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the response as a list of strings
        List<String> categories = response.jsonPath().getList("", String.class);
        log.info("Categories returned: {}", categories);

        // Must not be empty
        assertThat(categories).as("Categories list must not be empty").isNotEmpty();

        // Fake Store API has exactly these 4 categories in its seed data
        assertThat(categories)
                .as("Should contain all 4 expected Fake Store categories")
                .containsExactlyInAnyOrder(
                        "electronics",
                        "jewelery",
                        "men's clothing",
                        "women's clothing"
                );

        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED: GET /products/categories");
    }

    // ==================================================================
    // TEST 5 – GET /products/category/{category} – Products by Category
    // ==================================================================

    @Test
    @Order(5)
    @Story("Fetch products by category")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /products/category/electronics returns only electronics products.")
    @DisplayName("GET /products/category/electronics – returns filtered products")
    void testGetProductsByCategory_returnsOnlyCategoryProducts() {
        final String targetCategory = "electronics"; // category to filter by
        log.info(">>> TEST: GET /products/category/{}", targetCategory);

        // Send GET /products/category/electronics
        Response response = given()
                .spec(requestSpec)
                .pathParam("category", targetCategory) // substitute {category} in path
                .when()
                .get(Endpoints.PRODUCTS_BY_CATEGORY)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Deserialise the filtered product list
        List<Product> products = response.jsonPath().getList("", Product.class);
        log.info("Products in category '{}': {}", targetCategory, products.size());

        // The filtered list must not be empty
        assertThat(products).as("Category result must not be empty").isNotEmpty();

        // Every product in the response must belong to the requested category
        products.forEach(p ->
                assertThat(p.getCategory())
                        .as("Product %d category must be 'electronics'", p.getId())
                        .isEqualToIgnoringCase(targetCategory)
        );

        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED: GET /products/category/{}", targetCategory);
    }

    // ==================================================================
    // TEST 6 – POST /products – Create Product
    // ==================================================================

    @Test
    @Order(6)
    @Story("Create a new product")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify POST /products with a valid payload returns 200 and echoes the created product with a new ID.")
    @DisplayName("POST /products – creates product and returns ID in response")
    void testCreateProduct_returns200AndEchosNewProduct() {
        log.info(">>> TEST: POST /products – create new product");

        // Build the request payload using the centralised factory
        Product newProduct = TestDataFactory.buildNewProduct();
        log.info("Sending payload: {}", newProduct);

        // Send POST /products with the product JSON body
        Response response = given()
                .spec(requestSpec)
                .body(newProduct)                 // Jackson serialises the POJO to JSON
                .when()
                .post(Endpoints.PRODUCTS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 (not 201) for POST – this is known API behaviour
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract the echoed fields from the response
        Integer createdId    = response.jsonPath().getInt("id");
        String returnedTitle = response.jsonPath().getString("title");
        Double returnedPrice = response.jsonPath().getDouble("price");

        log.info("Created product – id:{}, title:'{}', price:{}", createdId, returnedTitle, returnedPrice);

        // The server must assign a positive integer ID
        assertThat(createdId).as("Created product must have an ID assigned by server").isNotNull();
        assertThat(createdId).as("Created product ID must be positive").isPositive();

        // The echoed title and price must match what we sent
        assertThat(returnedTitle).as("Echoed title must match sent title").isEqualTo(newProduct.getTitle());
        assertThat(returnedPrice).as("Echoed price must match sent price").isEqualTo(newProduct.getPrice());

        // Validate the create-response JSON Schema
        AssertionUtils.assertJsonSchema(response, "schemas/product-create-schema.json");
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: POST /products – product created with ID {}", createdId);
    }

    // ==================================================================
    // TEST 7 – PUT /products/{id} – Full Update
    // ==================================================================

    @Test
    @Order(7)
    @Story("Update an existing product")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify PUT /products/1 with updated payload returns 200 and echoes the updated fields.")
    @DisplayName("PUT /products/{id} – updates product and returns updated fields")
    void testUpdateProduct_returns200AndEchosUpdatedFields() {
        log.info(">>> TEST: PUT /products/{} – full update", EXISTING_PRODUCT_ID);

        // Build the update payload using the centralised factory
        Product updatedProduct = TestDataFactory.buildUpdatedProduct(EXISTING_PRODUCT_ID);
        log.info("Sending update payload: {}", updatedProduct);

        // Send PUT /products/1 with the updated product JSON body
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_PRODUCT_ID) // substitute {id} in the path
                .body(updatedProduct)
                .when()
                .put(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // Extract echoed fields from the response
        String returnedTitle = response.jsonPath().getString("title");
        Double returnedPrice = response.jsonPath().getDouble("price");
        int    returnedId    = response.jsonPath().getInt("id");

        log.info("Updated product echoed – id:{}, title:'{}', price:{}",
                returnedId, returnedTitle, returnedPrice);

        // The returned ID must match the one we updated
        assertThat(returnedId).as("Returned ID must match requested ID").isEqualTo(EXISTING_PRODUCT_ID);

        // The echoed title and price must reflect the update we sent
        assertThat(returnedTitle).as("Title should reflect the update").isEqualTo(updatedProduct.getTitle());
        assertThat(returnedPrice).as("Price should reflect the update").isEqualTo(updatedProduct.getPrice());

        // Validate the update-response JSON Schema
        AssertionUtils.assertJsonSchema(response, "schemas/product-update-schema.json");
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: PUT /products/{}", EXISTING_PRODUCT_ID);
    }

    // ==================================================================
    // TEST 8 – DELETE /products/{id} – Delete Product
    // ==================================================================

    @Test
    @Order(8)
    @Story("Delete an existing product")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify DELETE /products/1 returns 200 and echoes the deleted product's details.")
    @DisplayName("DELETE /products/{id} – deletes product and returns product details")
    void testDeleteProduct_returns200AndEchosProduct() {
        log.info(">>> TEST: DELETE /products/{} – delete product", EXISTING_PRODUCT_ID);

        // Send DELETE /products/1
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", EXISTING_PRODUCT_ID) // substitute {id} in the path
                .when()
                .delete(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // NOTE: Fake Store API returns 200 with the product object on DELETE (not 204)
        AssertionUtils.assertStatusCode(response, 200);

        // Assert Content-Type header
        AssertionUtils.assertContentType(response, "application/json");

        // The response body echoes the product that was deleted
        Integer deletedId   = response.jsonPath().getInt("id");
        String deletedTitle = response.jsonPath().getString("title");

        log.info("Deleted product echoed – id:{}, title:'{}'", deletedId, deletedTitle);

        // The echoed ID and title must be present and valid
        assertThat(deletedId).as("Deleted product ID must not be null").isNotNull();
        assertThat(deletedTitle).as("Deleted product title must not be blank").isNotBlank();

        // Validate the delete-response JSON Schema
        AssertionUtils.assertJsonSchema(response, "schemas/product-delete-schema.json");
        AssertionUtils.attachResponseBody(response);

        log.info("<<< TEST PASSED: DELETE /products/{}", EXISTING_PRODUCT_ID);
    }

    // ==================================================================
    // TEST 9 – Negative: Invalid Product ID
    // ==================================================================

    @Test
    @Order(9)
    @Story("Negative – Invalid product ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /products/{invalid-id} with a non-existent ID returns a null body (Fake Store behaviour).")
    @DisplayName("GET /products/{invalid-id} – non-existent ID returns null body")
    void testGetProductByInvalidId_returnsNullBody() {
        final int invalidId = 999999; // ID that does not exist in Fake Store seed data
        log.info(">>> TEST: GET /products/{} – invalid ID (negative test)", invalidId);

        // Send GET /products/999999
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", invalidId)
                .when()
                .get(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API returns 200 with body "null" for non-existent resources
        AssertionUtils.assertStatusCode(response, 200);

        // Read the raw body string and trim whitespace
        String body = response.body().asString().trim();
        log.info("Response body for invalid ID: '{}'", body);

        // Body must be "null" or empty – the API does not return 404
        assertThat(body)
                .as("Non-existent product should return null or empty body")
                .isIn("null", "");

        log.info("<<< TEST PASSED (Negative): GET /products/{}", invalidId);
    }

    // ==================================================================
    // TEST 10 – Negative: POST with missing required fields
    // ==================================================================

    @Test
    @Order(10)
    @Story("Negative – Create product with missing fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify POST /products with an empty body still returns a response (Fake Store echoes what it receives).")
    @DisplayName("POST /products – empty body returns response with null/missing fields")
    void testCreateProduct_withEmptyBody_returnsResponse() {
        log.info(">>> TEST: POST /products – empty body (negative test)");

        // Send POST with an empty JSON object – no title, price, etc.
        Response response = given()
                .spec(requestSpec)
                .body("{}")                        // empty JSON object as the request body
                .when()
                .post(Endpoints.PRODUCTS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API is lenient – it still returns 200 and echoes back what it received
        AssertionUtils.assertStatusCode(response, 200);

        // The response must still be JSON
        AssertionUtils.assertContentType(response, "application/json");

        // The server assigns an ID even for empty payloads
        Integer returnedId = response.jsonPath().getInt("id");
        log.info("Response for empty body – id:{}", returnedId);

        // ID must be present (server always assigns one)
        assertThat(returnedId).as("Server must still assign an ID even for empty payload").isNotNull();

        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED (Negative): POST /products with empty body");
    }

    // ==================================================================
    // TEST 11 – Negative: PUT with invalid ID
    // ==================================================================

    @Test
    @Order(11)
    @Story("Negative – Update product with invalid ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify PUT /products/{invalid-id} with a non-existent ID still returns 200 (Fake Store behaviour).")
    @DisplayName("PUT /products/{invalid-id} – non-existent ID returns 200")
    void testUpdateProduct_withInvalidId_returns200() {
        final int invalidId = 999999; // ID that does not exist in Fake Store seed data
        log.info(">>> TEST: PUT /products/{} – invalid ID (negative test)", invalidId);

        // Build a valid product payload to send with the invalid ID
        Product payload = TestDataFactory.buildUpdatedProduct(invalidId);

        // Send PUT /products/999999
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", invalidId)
                .body(payload)
                .when()
                .put(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API returns 200 even for non-existent IDs – it is a mock API
        AssertionUtils.assertStatusCode(response, 200);

        // The response must still be JSON
        AssertionUtils.assertContentType(response, "application/json");

        log.info("Response body for invalid PUT: {}", response.body().asString());
        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED (Negative): PUT /products/{}", invalidId);
    }

    // ==================================================================
    // TEST 12 – Negative: DELETE with invalid ID
    // ==================================================================

    @Test
    @Order(12)
    @Story("Negative – Delete product with invalid ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify DELETE /products/{invalid-id} with a non-existent ID still returns 200 (Fake Store behaviour).")
    @DisplayName("DELETE /products/{invalid-id} – non-existent ID returns 200")
    void testDeleteProduct_withInvalidId_returns200() {
        final int invalidId = 999999; // ID that does not exist in Fake Store seed data
        log.info(">>> TEST: DELETE /products/{} – invalid ID (negative test)", invalidId);

        // Send DELETE /products/999999
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", invalidId)
                .when()
                .delete(Endpoints.PRODUCT_BY_ID)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Fake Store API returns 200 even for non-existent IDs
        AssertionUtils.assertStatusCode(response, 200);

        // The response must still be JSON
        AssertionUtils.assertContentType(response, "application/json");

        log.info("Response body for invalid DELETE: {}", response.body().asString());
        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED (Negative): DELETE /products/{}", invalidId);
    }

    // ==================================================================
    // TEST 13 – GET /products?sort=desc – Sort Order
    // ==================================================================

    @Test
    @Order(13)
    @Story("Fetch products with sort order")
    @Severity(SeverityLevel.MINOR)
    @Description("Verify GET /products?sort=desc returns products in descending ID order.")
    @DisplayName("GET /products?sort=desc – returns products in descending ID order")
    void testGetAllProducts_sortedDesc_returnsDescendingOrder() {
        log.info(">>> TEST: GET /products?sort=desc – descending sort");

        // Send GET /products?sort=desc
        Response response = given()
                .spec(requestSpec)
                .queryParam("sort", "desc")       // attach sort query parameter
                .when()
                .get(Endpoints.PRODUCTS)
                .then()
                .spec(responseSpec)
                .extract().response();

        // Assert HTTP 200 OK
        AssertionUtils.assertStatusCode(response, 200);

        // Extract the list of product IDs from the response
        List<Integer> ids = response.jsonPath().getList("id", Integer.class);
        log.info("Product IDs in desc order (first 5): {}", ids.subList(0, Math.min(5, ids.size())));

        // IDs must be in descending order (each ID must be >= the next)
        assertThat(ids).as("IDs should be in descending order").isSortedAccordingTo((a, b) -> b - a);

        AssertionUtils.attachResponseBody(response);
        log.info("<<< TEST PASSED: GET /products?sort=desc");
    }
}
