package com.farez.nomina_backend.models.auth;

import com.farez.nomina_backend.models.entity.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private BigDecimal baseSalary;
    private Boolean active = true;
    private String username;
    private String password;
}
