package com.example.couponmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class Coupon {

    private String code;
    private String description;

    public enum DiscountType {
        FLAT,
        PERCENT
    }

    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endDate;

    private Integer usageLimitPerUser;

    private Eligibility eligibility;
}
