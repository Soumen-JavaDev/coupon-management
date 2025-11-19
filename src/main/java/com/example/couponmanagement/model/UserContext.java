package com.example.couponmanagement.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserContext {

    private String userId;
    private String userTier;
    private String country;
    private BigDecimal lifetimeSpend;
    private Integer ordersPlaced;
}
