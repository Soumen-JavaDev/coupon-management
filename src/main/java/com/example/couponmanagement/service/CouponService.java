package com.example.couponmanagement.service;

import com.example.couponmanagement.model.*;
import com.example.couponmanagement.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles coupon creation, validation, discount calculation,
 * and selection of best coupon.
 */
@Service
public class CouponService {

    private final InMemoryStore store;

    public CouponService(InMemoryStore store) {
        this.store = store;
    }

    /** Add coupon to storage */
    public boolean createCoupon(Coupon coupon) {
        return store.addCoupon(coupon);
    }

    /** Return all coupons */
    public List<Coupon> getAllCoupons() {
        return store.getCoupons();
    }

    /** Simple structure for best coupon + discount */
    public static class BestCouponResult {
        public Coupon coupon;
        public BigDecimal discount;

        public BestCouponResult(Coupon c, BigDecimal d) {
            this.coupon = c;
            this.discount = d;
        }
    }

    /** Main method to compute best coupon */
    public BestCouponResult findBestCoupon(UserContext user, Cart cart) {

        OffsetDateTime now = OffsetDateTime.now();

        List<Coupon> validCoupons = store.getCoupons().stream()
                .filter(c -> isValidDate(c, now))
                .filter(c -> !exceededUsage(c, user))
                .filter(c -> satisfiesEligibility(c, user, cart))
                .collect(Collectors.toList());

        List<Map.Entry<Coupon, BigDecimal>> discountList = validCoupons.stream()
                .map(c -> Map.entry(c, calculateDiscount(c, cart)))
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (discountList.isEmpty()) return null;

        // sort highest discount → earliest expiry → alphabetical
        discountList.sort((a, b) -> {
            int d = b.getValue().compareTo(a.getValue());
            if (d != 0) return d;

            int dateCmp = a.getKey().getEndDate().compareTo(b.getKey().getEndDate());
            if (dateCmp != 0) return dateCmp;

            return a.getKey().getCode().compareTo(b.getKey().getCode());
        });

        Map.Entry<Coupon, BigDecimal> best = discountList.get(0);

        return new BestCouponResult(best.getKey(), best.getValue());
    }

    private boolean isValidDate(Coupon c, OffsetDateTime now) {
        return (c.getStartDate() == null || !now.isBefore(c.getStartDate()))
                && (c.getEndDate() == null || !now.isAfter(c.getEndDate()));
    }

    private boolean exceededUsage(Coupon c, UserContext user) {
        if (c.getUsageLimitPerUser() == null) return false;
        if (user == null || user.getUserId() == null) return false;

        int used = store.getUsageCount(user.getUserId(), c.getCode());

        return used >= c.getUsageLimitPerUser();
    }


    private boolean satisfiesEligibility(Coupon c, UserContext user, Cart cart) {
        Eligibility e = c.getEligibility();
        if (e == null) return true;

        if (e.getAllowedUserTiers() != null && !e.getAllowedUserTiers().isEmpty()) {
            if (!e.getAllowedUserTiers().contains(user.getUserTier())) return false;
        }

        if (e.getMinLifetimeSpend() != null &&
                user.getLifetimeSpend().compareTo(e.getMinLifetimeSpend()) < 0) {
            return false;
        }

        if (e.getMinOrdersPlaced() != null &&
                user.getOrdersPlaced() < e.getMinOrdersPlaced()) {
            return false;
        }

        if (Boolean.TRUE.equals(e.getFirstOrderOnly()) && user.getOrdersPlaced() > 0) {
            return false;
        }

        if (e.getAllowedCountries() != null && !e.getAllowedCountries().isEmpty()) {
            if (!e.getAllowedCountries().contains(user.getCountry())) return false;
        }

        if (e.getMinCartValue() != null &&
                cart.cartValue().compareTo(e.getMinCartValue()) < 0) {
            return false;
        }

        if (e.getMinItemsCount() != null &&
                cart.totalItemsCount() < e.getMinItemsCount()) {
            return false;
        }

        return true;
    }

    private BigDecimal calculateDiscount(Coupon c, Cart cart) {
        BigDecimal cartValue = cart.cartValue();

        if (c.getDiscountType() == Coupon.DiscountType.FLAT) {
            return c.getDiscountValue().min(cartValue);
        }

        BigDecimal percent = cartValue.multiply(c.getDiscountValue())
                .divide(BigDecimal.valueOf(100));

        if (c.getMaxDiscountAmount() != null) {
            percent = percent.min(c.getMaxDiscountAmount());
        }

        return percent.min(cartValue);
    }

    /** Increment usage */
    public void recordUsage(String userId, String code) {
        store.incrementUsage(userId, code);
    }

    /** Expose usage for controller */
    public int getUsageCount(String userId, String code) {
        return store.getUsageCount(userId, code);
    }
}
