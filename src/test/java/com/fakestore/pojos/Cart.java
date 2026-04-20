package com.fakestore.pojos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Cart – POJO that mirrors the Fake Store API cart resource.
 *
 * Sample API response:
 * {
 *   "id": 1,
 *   "userId": 1,
 *   "date": "2020-03-02T00:00:00.000Z",
 *   "products": [
 *     { "productId": 1, "quantity": 4 }
 *   ]
 * }
 *
 * Annotations:
 *   @JsonIgnoreProperties(ignoreUnknown = true) – silently ignores any extra fields the API adds.
 *   @JsonInclude(NON_NULL) – omits null fields when serialising a request body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cart {

    // Unique cart identifier assigned by the server
    @JsonProperty("id")
    private Integer id;

    // ID of the user who owns this cart
    @JsonProperty("userId")
    private Integer userId;

    // ISO-8601 date string when the cart was created/updated
    @JsonProperty("date")
    private String date;

    // List of products (productId + quantity) in this cart
    @JsonProperty("products")
    private List<CartProduct> products;

    // ----------------------------------------------------------------
    // No-arg constructor required by Jackson for deserialisation
    // ----------------------------------------------------------------
    public Cart() {}

    // ----------------------------------------------------------------
    // Builder-style constructor for creating cart request payloads
    // ----------------------------------------------------------------
    public Cart(Integer userId, String date, List<CartProduct> products) {
        this.userId   = userId;   // owner of the cart
        this.date     = date;     // cart date in ISO-8601 format
        this.products = products; // list of products in the cart
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    /** Returns the cart's unique ID. */
    public Integer getId()           { return id; }
    /** Sets the cart's unique ID. */
    public void setId(Integer id)    { this.id = id; }

    /** Returns the ID of the user who owns this cart. */
    public Integer getUserId()              { return userId; }
    /** Sets the user ID for this cart. */
    public void setUserId(Integer userId)   { this.userId = userId; }

    /** Returns the cart date string. */
    public String getDate()             { return date; }
    /** Sets the cart date string. */
    public void setDate(String date)    { this.date = date; }

    /** Returns the list of products in this cart. */
    public List<CartProduct> getProducts()                  { return products; }
    /** Sets the list of products in this cart. */
    public void setProducts(List<CartProduct> products)     { this.products = products; }

    @Override
    public String toString() {
        return "Cart{id=" + id + ", userId=" + userId + ", date='" + date
                + "', products=" + products + "}";
    }

    // ----------------------------------------------------------------
    // Inner class: CartProduct
    // Represents a single product entry inside a cart (productId + quantity).
    // ----------------------------------------------------------------

    /**
     * CartProduct – nested object representing one product line in a cart.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartProduct {

        // The ID of the product being added to the cart
        @JsonProperty("productId")
        private Integer productId;

        // How many units of this product are in the cart
        @JsonProperty("quantity")
        private Integer quantity;

        // No-arg constructor required by Jackson
        public CartProduct() {}

        // Convenience constructor for building cart payloads
        public CartProduct(Integer productId, Integer quantity) {
            this.productId = productId; // which product
            this.quantity  = quantity;  // how many
        }

        /** Returns the product ID. */
        public Integer getProductId()               { return productId; }
        /** Sets the product ID. */
        public void setProductId(Integer productId) { this.productId = productId; }

        /** Returns the quantity. */
        public Integer getQuantity()                { return quantity; }
        /** Sets the quantity. */
        public void setQuantity(Integer quantity)   { this.quantity = quantity; }

        @Override
        public String toString() {
            return "CartProduct{productId=" + productId + ", quantity=" + quantity + "}";
        }
    }
}
