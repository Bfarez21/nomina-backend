package com.farez.nomina_backend.controllers.auth;

import com.farez.nomina_backend.models.auth.AuthResponse;
import com.farez.nomina_backend.models.auth.LoginRequest;
import com.farez.nomina_backend.models.auth.RegisterRequest;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final userRepository userRepository;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse>signIn(@RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);
        User user =userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));
        authResponse.setUsuario(user);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    private ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        if (authService.isEmailAlreadyRegistered(request.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse("correo"+ request.getUsername() + " ya registrado", null));
        }
        return ResponseEntity.ok(authService.regiter(request));
    }
}
