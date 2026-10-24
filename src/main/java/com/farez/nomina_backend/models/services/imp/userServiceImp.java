package com.farez.nomina_backend.models.services.imp;

import com.farez.nomina_backend.dto.userCreateRequest;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.User;
import com.farez.nomina_backend.models.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class userServiceImp implements IUserService {
    @Autowired
    private userRepository userRepository;

    @Override
    @Transactional
    public User createUser(userCreateRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .baseSalary(request.getBaseSalary())
                .active(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }
}
