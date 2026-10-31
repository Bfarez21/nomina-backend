package com.farez.nomina_backend.models.dao;

import com.farez.nomina_backend.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface userRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String correo);
}
