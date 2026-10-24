package com.farez.nomina_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class userCreateRequest {
    private String firstName;
    private String lastName;
    private BigDecimal baseSalary;
}
