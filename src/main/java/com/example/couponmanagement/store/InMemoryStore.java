package com.example.couponmanagement.store;

import com.example.couponmanagement.model.Coupon;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStore {

    private final Map<String, Coupon> couponMap = new ConcurrentHashMap<>();

    // THIS WAS THE REAL ISSUE — USE CORRECT MAP STRUCTURE
    private final Map<String, Map<String, Integer>> usageMap = new ConcurrentHashMap<>();

    public boolean addCoupon(Coupon coupon) {
        return couponMap.putIfAbsent(coupon.getCode(), coupon) == null;
    }

    public List<Coupon> getCoupons() {
        return new ArrayList<>(couponMap.values());
    }

    public int getUsageCount(String userId, String couponCode) {
        return usageMap
                .getOrDefault(userId, new ConcurrentHashMap<>())
                .getOrDefault(couponCode, 0);
    }

    public void incrementUsage(String userId, String couponCode) {


        usageMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());


        usageMap.get(userId).merge(couponCode, 1, Integer::sum);
    }
}
