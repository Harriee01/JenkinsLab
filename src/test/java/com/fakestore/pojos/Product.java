package com.fakestore.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Product – POJO that mirrors the Fake Store API product resource.
 *
 * <p>Annotations:
 * <ul>
 *   <li>{@code @JsonIgnoreProperties(ignoreUnknown = true)} – future-proofs
 *       the model; unknown fields from the API are silently ignored.</li>
 *   <li>{@code @JsonInclude(NON_NULL)} – omits null fields when serialising
 *       a request body (e.g. for CREATE / UPDATE operations).</li>
 * </ul>
 *
 * Sample API response:
 * <pre>{@code
 * {
 *   "id": 1,
 *   "title": "Fjallraven Backpack",
 *   "price": 109.95,
 *   "description": "Your perfect pack ...",
 *   "category": "men's clothing",
 *   "image": "https://fakestoreapi.com/img/81fAn...",
 *   "rating": { "rate": 3.9, "count": 120 }
 * }
 * }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("image")
    private String image;

    @JsonProperty("rating")
    private Rating rating;

    // ----------------------------------------------------------------
    // No-arg constructor required by Jackson
    // ----------------------------------------------------------------
    public Product() {}

    // ----------------------------------------------------------------
    // Builder-style constructor – handy for building request payloads
    // ----------------------------------------------------------------
    public Product(String title, Double price, String description, String category, String image) {
        this.title       = title;
        this.price       = price;
        this.description = description;
        this.category    = category;
        this.image       = image;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    public Integer getId()            { return id; }
    public void setId(Integer id)     { this.id = id; }

    public String getTitle()                { return title; }
    public void setTitle(String title)      { this.title = title; }

    public Double getPrice()                { return price; }
    public void setPrice(Double price)      { this.price = price; }

    public String getDescription()               { return description; }
    public void setDescription(String description){ this.description = description; }

    public String getCategory()               { return category; }
    public void setCategory(String category)  { this.category = category; }

    public String getImage()             { return image; }
    public void setImage(String image)   { this.image = image; }

    public Rating getRating()              { return rating; }
    public void setRating(Rating rating)   { this.rating = rating; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", title='" + title + "', price=" + price
                + ", category='" + category + "'}";
    }

    // ----------------------------------------------------------------
    // Inner class: Rating
    // ----------------------------------------------------------------

    /**
     * Rating – nested object returned by the API inside each product.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Rating {

        @JsonProperty("rate")
        private Double rate;

        @JsonProperty("count")
        private Integer count;

        public Rating() {}

        public Double getRate()              { return rate; }
        public void setRate(Double rate)     { this.rate = rate; }

        public Integer getCount()            { return count; }
        public void setCount(Integer count)  { this.count = count; }

        @Override
        public String toString() {
            return "Rating{rate=" + rate + ", count=" + count + "}";
        }
    }
}
