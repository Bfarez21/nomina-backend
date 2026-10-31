package com.farez.nomina_backend.controllers;

import com.farez.nomina_backend.dto.userCreateRequest;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.User;
import com.farez.nomina_backend.models.services.IUserService;
import exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class userController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private userRepository userRepository;


    //listar user
    @GetMapping
    public List<User> getAllUsuarios() {
        return userRepository.findAll();
    }

    // buscar por id
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    //crear
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

}