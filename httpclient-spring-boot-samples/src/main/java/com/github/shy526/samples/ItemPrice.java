package com.github.shy526.samples;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemPrice {


    private BigDecimal maxPrice;
    private BigDecimal minPrice;

    private BigDecimal avgPrice;

    private BigDecimal num;
}
