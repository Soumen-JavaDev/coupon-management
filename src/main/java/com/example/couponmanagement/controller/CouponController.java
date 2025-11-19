package com.example.couponmanagement.controller;

import com.example.couponmanagement.model.Cart;
import com.example.couponmanagement.model.UserContext;
import com.example.couponmanagement.model.Coupon;
import com.example.couponmanagement.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller for coupon operations.
 */
@RestController
@RequestMapping("/api")
public class CouponController {

    private final CouponService couponService;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("error", "Coupon code required"));
        }

        boolean ok = couponService.createCoupon(coupon);
        if (!ok) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Coupon already exists"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> listCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/coupons/best")
    public ResponseEntity<?> bestCoupon(@RequestBody Map<String, Object> body) {

        UserContext user = mapper.convertValue(body.get("user"), UserContext.class);

        // FIX: Avoid null cart crash (NO Map.of)
        Object cartObj = body.getOrDefault(
                "cart",
                Collections.singletonMap("items", List.of())
        );

        Cart cart = mapper.convertValue(cartObj, Cart.class);

        CouponService.BestCouponResult result = couponService.findBestCoupon(user, cart);

        if (result == null) {
            // FIX: Map.of cannot take null → use singletonMap
            return ResponseEntity.ok(Collections.singletonMap("bestCoupon", null));
        }

        return ResponseEntity.ok(Map.of(
                "bestCoupon", result.coupon,
                "discountAmount", result.discount
        ));
    }


    @PostMapping("/coupons/redeem")
    public ResponseEntity<?> redeem(@RequestBody Map<String, String> body) {

        String userId = body.get("userId");
        String code = body.get("code");

        couponService.recordUsage(userId, code);
        int usage = couponService.getUsageCount(userId, code);

        return ResponseEntity.ok(Map.of(
                "status", "redeemed",
                "userId", userId,
                "code", code,
                "usageCount", usage
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
