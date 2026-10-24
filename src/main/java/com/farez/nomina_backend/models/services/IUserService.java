package com.farez.nomina_backend.models.services;

import com.farez.nomina_backend.dto.userCreateRequest;
import com.farez.nomina_backend.models.entity.User;

import java.util.List;

public interface IUserService {
    User createUser(userCreateRequest request);
    List<User> getAllUsers();
    User getUserById(Long id);
}
