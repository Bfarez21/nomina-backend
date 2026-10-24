package com.farez.nomina_backend.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.farez.nomina_backend.models.entity.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "workDate"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class timeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private user user;

    @Column(nullable = false)
    private LocalDate workDate;

    private LocalDateTime clockIn; // entrada
    private LocalDateTime lunchOut; //salida almuerzo
    private LocalDateTime lunchIn; //entrada almuerzo
    private LocalDateTime clockOut; //salida
}
