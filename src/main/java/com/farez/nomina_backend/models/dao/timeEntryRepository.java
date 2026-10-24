package com.farez.nomina_backend.models.dao;

import com.farez.nomina_backend.models.entity.timeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface timeEntryRepository extends JpaRepository<timeEntry, Long> {
    // busca marcaciones en fecha dada
    Optional<timeEntry> findByUserIdAndWorkDate(Long userId, LocalDate workDate);
}
