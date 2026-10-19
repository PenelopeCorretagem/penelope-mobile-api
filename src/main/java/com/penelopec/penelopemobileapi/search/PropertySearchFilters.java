package com.penelopec.penelopemobileapi.search;

import java.math.BigDecimal;

public record PropertySearchFilters(
    String searchTerm,
    String city,
    String region,
    String type,
    Integer minBedrooms,
    BigDecimal maxPrice,
    String sortOrder
) {
    public static PropertySearchFilters defaults() {
        return new PropertySearchFilters("", null, null, "TODOS", null, null, "distance");
    }
}
