package com.fakestore.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fakestore.pojos.Cart;
import com.fakestore.pojos.Cart.CartProduct;
import com.fakestore.pojos.Product;
import com.fakestore.pojos.User;
import com.fakestore.pojos.User.Address;
import com.fakestore.pojos.User.Geolocation;
import com.fakestore.pojos.User.Name;

import io.qameta.allure.Step;

/**
 * TestDataFactory – centralised factory for all test payload creation.
 *
 * Keeps test classes free of inline object construction so payloads
 * can be discovered, updated, or templatised in one place.
 *
 * Every factory method is annotated @Step so Allure reports show
 * exactly what data was used in each test scenario.
 */
public final class TestDataFactory {

    // Logger for this utility class
    private static final Logger log = LoggerFactory.getLogger(TestDataFactory.class);

    // Utility class – prevent instantiation
    private TestDataFactory() {}

    // ================================================================
    // Product payloads
    // ================================================================

    /**
     * Builds a valid Product payload for a CREATE (POST) request.
     * No ID is set – the server assigns one.
     *
     * @return a fully populated Product without an ID
     */
    @Step("Build valid new Product payload")
    public static Product buildNewProduct() {
        // Construct a product with all required fields populated
        Product product = new Product(
                "Automation Test Backpack",   // title – descriptive name for the test product
                129.99,                        // price – positive double value
                "A durable backpack created by the automation test suite.", // description
                "men's clothing",              // category – must be a real Fake Store category
                "https://fakestoreapi.com/img/81fAnIKqBNL._AC_UX679_.jpg"  // image URL
        );
        log.info("Built new Product payload: {}", product);
        return product;
    }

    /**
     * Builds a valid Product payload for an UPDATE (PUT) request.
     *
     * @param id the ID of the product to update (echoed back by the server)
     * @return an updated Product with modified title and price
     */
    @Step("Build updated Product payload for ID {id}")
    public static Product buildUpdatedProduct(int id) {
        // Construct a product with updated title and price
        Product product = new Product(
                "Updated Automation Backpack",  // updated title
                149.99,                          // updated price
                "Updated description from automation test suite.", // updated description
                "men's clothing",                // category stays the same
                "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_.jpg" // updated image
        );
        product.setId(id); // set the ID so the server knows which product to update
        log.info("Built updated Product payload: {}", product);
        return product;
    }

    /**
     * Builds a partial Product payload (used for PATCH-like behaviour via PUT).
     * Only title, price, category, description, and image are set – no ID.
     *
     * @return a Product with all fields except ID
     */
    @Step("Build partial Product payload (title only)")
    public static Product buildPartialProduct() {
        // Construct a product with only some fields set
        Product product = new Product();
        product.setTitle("Partially Updated Title");       // only title is being changed
        product.setPrice(59.99);                           // updated price
        product.setCategory("electronics");                // different category
        product.setDescription("Partial update description"); // updated description
        product.setImage("https://fakestoreapi.com/img/81QpkIctqPL._AC_SX679_.jpg"); // updated image
        log.info("Built partial Product payload: {}", product);
        return product;
    }

    // ================================================================
    // Cart payloads
    // ================================================================

    /**
     * Builds a valid Cart payload for a CREATE (POST /carts) request.
     * Uses real product IDs from Fake Store seed data.
     *
     * @return a fully populated Cart without an ID (server assigns one)
     */
    @Step("Build valid new Cart payload")
    public static Cart buildNewCart() {
        // Create a list of products to add to the cart
        List<CartProduct> products = List.of(
                new CartProduct(1, 3),  // 3 units of product ID 1
                new CartProduct(2, 1)   // 1 unit of product ID 2
        );
        // Construct the cart with a user ID, date, and product list
        Cart cart = new Cart(
                1,                    // userId – must be a real Fake Store user ID
                "2024-01-15",         // date in ISO-8601 format (YYYY-MM-DD)
                products              // list of CartProduct objects
        );
        log.info("Built new Cart payload: {}", cart);
        return cart;
    }

    /**
     * Builds a valid Cart payload for an UPDATE (PUT /carts/{id}) request.
     *
     * @param id the ID of the cart to update
     * @return an updated Cart with modified product quantities
     */
    @Step("Build updated Cart payload for ID {id}")
    public static Cart buildUpdatedCart(int id) {
        // Create an updated product list with different quantities
        List<CartProduct> products = List.of(
                new CartProduct(1, 5),  // increased quantity of product ID 1
                new CartProduct(3, 2)   // added product ID 3 with quantity 2
        );
        // Construct the updated cart
        Cart cart = new Cart(
                1,             // userId stays the same
                "2024-02-20",  // updated date
                products       // updated product list
        );
        cart.setId(id); // set the ID so the server knows which cart to update
        log.info("Built updated Cart payload: {}", cart);
        return cart;
    }

    // ================================================================
    // User payloads
    // ================================================================

    /**
     * Builds a valid User payload for a CREATE (POST /users) request.
     *
     * @return a fully populated User without an ID (server assigns one)
     */
    @Step("Build valid new User payload")
    public static User buildNewUser() {
        // Build the geolocation object (lat/long coordinates)
        Geolocation geo = new Geolocation("-37.3159", "81.1496");

        // Build the address object with all required fields
        Address address = new Address(
                "kilcoole",          // city
                "new road",          // street
                7835,                // street number
                "12926-3874",        // zipcode
                geo                  // geolocation coordinates
        );

        // Build the name object (first + last name)
        Name name = new Name("Test", "Automation");

        // Construct the full user object
        User user = new User(
                "testautomation@example.com", // email – unique test email
                "testautomation",              // username – unique test username
                "TestPass123!",                // password – plain text (Fake Store API)
                name,                          // full name object
                address,                       // address object
                "1-800-TEST-AUTO"              // phone number
        );
        log.info("Built new User payload: {}", user);
        return user;
    }

    /**
     * Builds a valid User payload for an UPDATE (PUT /users/{id}) request.
     *
     * @param id the ID of the user to update
     * @return an updated User with modified email and phone
     */
    @Step("Build updated User payload for ID {id}")
    public static User buildUpdatedUser(int id) {
        // Build the geolocation object
        Geolocation geo = new Geolocation("-37.3159", "81.1496");

        // Build the address object
        Address address = new Address(
                "newcity",           // updated city
                "updated street",    // updated street
                100,                 // updated street number
                "99999",             // updated zipcode
                geo                  // same geolocation
        );

        // Build the updated name object
        Name name = new Name("Updated", "User");

        // Construct the updated user object
        User user = new User(
                "updated@example.com", // updated email
                "updateduser",          // updated username
                "NewPass456!",          // updated password
                name,                   // updated name
                address,                // updated address
                "1-999-UPDATED"         // updated phone
        );
        user.setId(id); // set the ID so the server knows which user to update
        log.info("Built updated User payload: {}", user);
        return user;
    }
}
