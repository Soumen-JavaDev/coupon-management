package com.example.couponmanagement.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {

    private String productId;
    private String category;
    private BigDecimal unitPrice;
    private int quantity;
}
