package com.example.couponmanagement.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private List<CartItem> items;

    public BigDecimal cartValue() {
        if (items == null) return BigDecimal.ZERO;

        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int totalItemsCount() {
        if (items == null) return 0;

        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
