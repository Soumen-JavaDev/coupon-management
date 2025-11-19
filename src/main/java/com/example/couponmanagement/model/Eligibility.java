package com.example.couponmanagement.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Eligibility {

    // User-based conditions
    private List<String> allowedUserTiers;
    private BigDecimal minLifetimeSpend;
    private Integer minOrdersPlaced;
    private Boolean firstOrderOnly;
    private List<String> allowedCountries;

    // Cart-based conditions
    private BigDecimal minCartValue;
    private List<String> applicableCategories;
    private List<String> excludedCategories;
    private Integer minItemsCount;
}
