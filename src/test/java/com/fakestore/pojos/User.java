package com.fakestore.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User – POJO that mirrors the Fake Store API user resource.
 *
 * Sample API response:
 * {
 *   "id": 1,
 *   "email": "john@gmail.com",
 *   "username": "johnd",
 *   "password": "m38rmF$",
 *   "name": {
 *     "firstname": "john",
 *     "lastname": "doe"
 *   },
 *   "address": {
 *     "city": "kilcoole",
 *     "street": "7835 new road",
 *     "number": 3,
 *     "zipcode": "12926-3874",
 *     "geolocation": {
 *       "lat": "-37.3159",
 *       "long": "81.1496"
 *     }
 *   },
 *   "phone": "1-570-236-7033"
 * }
 *
 * Annotations:
 *   @JsonIgnoreProperties(ignoreUnknown = true) – silently ignores any extra fields the API adds.
 *   @JsonInclude(NON_NULL) – omits null fields when serialising a request body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    // Unique user identifier assigned by the server
    @JsonProperty("id")
    private Integer id;

    // User's email address
    @JsonProperty("email")
    private String email;

    // User's login username
    @JsonProperty("username")
    private String username;

    // User's password (plain text in Fake Store API – not a real production pattern)
    @JsonProperty("password")
    private String password;

    // User's full name (nested object: firstname + lastname)
    @JsonProperty("name")
    private Name name;

    // User's address (nested object with city, street, geolocation, etc.)
    @JsonProperty("address")
    private Address address;

    // User's phone number
    @JsonProperty("phone")
    private String phone;

    // ----------------------------------------------------------------
    // No-arg constructor required by Jackson for deserialisation
    // ----------------------------------------------------------------
    public User() {}

    // ----------------------------------------------------------------
    // Builder-style constructor for creating user request payloads
    // ----------------------------------------------------------------
    public User(String email, String username, String password, Name name, Address address, String phone) {
        this.email    = email;    // user's email
        this.username = username; // login username
        this.password = password; // plain text password
        this.name     = name;     // full name object
        this.address  = address;  // address object
        this.phone    = phone;    // phone number
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    /** Returns the user's unique ID. */
    public Integer getId()           { return id; }
    /** Sets the user's unique ID. */
    public void setId(Integer id)    { this.id = id; }

    /** Returns the user's email. */
    public String getEmail()             { return email; }
    /** Sets the user's email. */
    public void setEmail(String email)   { this.email = email; }

    /** Returns the user's username. */
    public String getUsername()                { return username; }
    /** Sets the user's username. */
    public void setUsername(String username)   { this.username = username; }

    /** Returns the user's password. */
    public String getPassword()                { return password; }
    /** Sets the user's password. */
    public void setPassword(String password)   { this.password = password; }

    /** Returns the user's name object. */
    public Name getName()          { return name; }
    /** Sets the user's name object. */
    public void setName(Name name) { this.name = name; }

    /** Returns the user's address object. */
    public Address getAddress()              { return address; }
    /** Sets the user's address object. */
    public void setAddress(Address address)  { this.address = address; }

    /** Returns the user's phone number. */
    public String getPhone()             { return phone; }
    /** Sets the user's phone number. */
    public void setPhone(String phone)   { this.phone = phone; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', username='" + username + "'}";
    }

    // ----------------------------------------------------------------
    // Inner class: Name
    // Represents the user's full name (firstname + lastname).
    // ----------------------------------------------------------------

    /**
     * Name – nested object inside User representing first and last name.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Name {

        // User's first name
        @JsonProperty("firstname")
        private String firstname;

        // User's last name
        @JsonProperty("lastname")
        private String lastname;

        // No-arg constructor required by Jackson
        public Name() {}

        // Convenience constructor
        public Name(String firstname, String lastname) {
            this.firstname = firstname; // first name
            this.lastname  = lastname;  // last name
        }

        /** Returns the first name. */
        public String getFirstname()                 { return firstname; }
        /** Sets the first name. */
        public void setFirstname(String firstname)   { this.firstname = firstname; }

        /** Returns the last name. */
        public String getLastname()                { return lastname; }
        /** Sets the last name. */
        public void setLastname(String lastname)   { this.lastname = lastname; }

        @Override
        public String toString() {
            return "Name{firstname='" + firstname + "', lastname='" + lastname + "'}";
        }
    }

    // ----------------------------------------------------------------
    // Inner class: Address
    // Represents the user's address (city, street, number, zipcode, geolocation).
    // ----------------------------------------------------------------

    /**
     * Address – nested object inside User representing physical address.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {

        // City name
        @JsonProperty("city")
        private String city;

        // Street name
        @JsonProperty("street")
        private String street;

        // Street number
        @JsonProperty("number")
        private Integer number;

        // Postal/ZIP code
        @JsonProperty("zipcode")
        private String zipcode;

        // Geolocation coordinates (nested object: lat + long)
        @JsonProperty("geolocation")
        private Geolocation geolocation;

        // No-arg constructor required by Jackson
        public Address() {}

        // Convenience constructor
        public Address(String city, String street, Integer number, String zipcode, Geolocation geolocation) {
            this.city        = city;        // city name
            this.street      = street;      // street name
            this.number      = number;      // street number
            this.zipcode     = zipcode;     // postal code
            this.geolocation = geolocation; // lat/long coordinates
        }

        /** Returns the city. */
        public String getCity()            { return city; }
        /** Sets the city. */
        public void setCity(String city)   { this.city = city; }

        /** Returns the street. */
        public String getStreet()              { return street; }
        /** Sets the street. */
        public void setStreet(String street)   { this.street = street; }

        /** Returns the street number. */
        public Integer getNumber()               { return number; }
        /** Sets the street number. */
        public void setNumber(Integer number)    { this.number = number; }

        /** Returns the zipcode. */
        public String getZipcode()               { return zipcode; }
        /** Sets the zipcode. */
        public void setZipcode(String zipcode)   { this.zipcode = zipcode; }

        /** Returns the geolocation object. */
        public Geolocation getGeolocation()                    { return geolocation; }
        /** Sets the geolocation object. */
        public void setGeolocation(Geolocation geolocation)    { this.geolocation = geolocation; }

        @Override
        public String toString() {
            return "Address{city='" + city + "', street='" + street + "', number=" + number + "}";
        }
    }

    // ----------------------------------------------------------------
    // Inner class: Geolocation
    // Represents latitude and longitude coordinates.
    // ----------------------------------------------------------------

    /**
     * Geolocation – nested object inside Address representing lat/long coordinates.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Geolocation {

        // Latitude coordinate (string in Fake Store API)
        @JsonProperty("lat")
        private String lat;

        // Longitude coordinate (string in Fake Store API)
        @JsonProperty("long")
        private String longitude;

        // No-arg constructor required by Jackson
        public Geolocation() {}

        // Convenience constructor
        public Geolocation(String lat, String longitude) {
            this.lat       = lat;       // latitude
            this.longitude = longitude; // longitude
        }

        /** Returns the latitude. */
        public String getLat()           { return lat; }
        /** Sets the latitude. */
        public void setLat(String lat)   { this.lat = lat; }

        /** Returns the longitude. */
        public String getLongitude()                 { return longitude; }
        /** Sets the longitude. */
        public void setLongitude(String longitude)   { this.longitude = longitude; }

        @Override
        public String toString() {
            return "Geolocation{lat='" + lat + "', long='" + longitude + "'}";
        }
    }
}
