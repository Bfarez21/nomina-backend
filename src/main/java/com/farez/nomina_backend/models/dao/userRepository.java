package com.farez.nomina_backend.models.dao;

import com.farez.nomina_backend.models.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface userRepository extends JpaRepository<user, Long> {
}
