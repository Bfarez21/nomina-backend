package com.farez.nomina_backend.controllers.auth;

import com.farez.nomina_backend.jwt.JwtService;
import com.farez.nomina_backend.models.auth.AuthResponse;
import com.farez.nomina_backend.models.auth.LoginRequest;
import com.farez.nomina_backend.models.auth.RegisterRequest;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.Role;
import com.farez.nomina_backend.models.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final userRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    //login
    public AuthResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        UserDetails userDetails= userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow();
        String token = jwtService.getToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    //register
    public AuthResponse regiter(RegisterRequest request){
        User user=User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .baseSalary(request.getBaseSalary())
                .active(request.getActive())
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_EMPLOYEE)
                .build();
        userRepository.save(user);
        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }

    //verificar existecia de username
    public boolean isEmailAlreadyRegistered(String email){
        Optional<User> userOptional = userRepository.findByUsername(email);
        return userOptional.isPresent();
    }
}
