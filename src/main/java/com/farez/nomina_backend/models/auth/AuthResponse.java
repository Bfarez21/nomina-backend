package com.farez.nomina_backend.models.auth;

import com.farez.nomina_backend.models.entity.Role;
import com.farez.nomina_backend.models.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String username;
    private Role role;
}
