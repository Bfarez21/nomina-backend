package com.farez.nomina_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class clockInRequest {
    private Long userId;

    // si viene nulo toma la de ahora
    private LocalDateTime customTimestamp;
}
