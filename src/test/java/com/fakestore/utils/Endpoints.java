package com.fakestore.utils;

/**
 * Endpoints – compile-time constants for all Fake Store API endpoint paths.
 *
 * <p>Centralising path strings here means a URL change in the API only
 * needs to be updated in one location, not scattered across every test class.
 *
 * <p>Paths are relative to the base URL configured in {@link com.fakestore.config.AppConfig}.
 *
 * <p>API reference: https://fakestoreapi.com/docs
 */
public final class Endpoints {

    // Utility class – not instantiatable
    private Endpoints() {}

    // ================================================================
    // Products  ──  https://fakestoreapi.com/products
    // ================================================================

    /** Base path for all product operations. */
    public static final String PRODUCTS             = "/products";

    /** Path to fetch a single product. Use with {@code .pathParam("id", id)}. */
    public static final String PRODUCT_BY_ID        = "/products/{id}";

    /** Path to retrieve all available product categories. */
    public static final String PRODUCT_CATEGORIES   = "/products/categories";

    /** Path to retrieve products filtered by category. Use with {@code .pathParam("category", name)}. */
    public static final String PRODUCTS_BY_CATEGORY = "/products/category/{category}";

    // ================================================================
    // Carts  ──  https://fakestoreapi.com/carts
    // ================================================================

    /** Base path for all cart operations. */
    public static final String CARTS                = "/carts";

    /** Path to fetch a single cart by ID. */
    public static final String CART_BY_ID           = "/carts/{id}";

    /** Path to fetch carts belonging to a specific user. */
    public static final String CARTS_BY_USER        = "/carts/user/{userId}";

    // ================================================================
    // Users  ──  https://fakestoreapi.com/users
    // ================================================================

    /** Base path for all user operations. */
    public static final String USERS                = "/users";

    /** Path to fetch a single user by ID. */
    public static final String USER_BY_ID           = "/users/{id}";

    // ================================================================
    // Auth  ──  https://fakestoreapi.com/auth/login
    // ================================================================

    /** Path for obtaining a JWT token. Expects {@code username} + {@code password} body. */
    public static final String AUTH_LOGIN           = "/auth/login";
}
