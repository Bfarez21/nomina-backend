package com.farez.nomina_backend.controllers;

import com.farez.nomina_backend.dto.clockInRequest;
import com.farez.nomina_backend.models.entity.timeEntry;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.farez.nomina_backend.models.services.ITimeEntryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class attendanceController {

    @Autowired
    private ITimeEntryService timeEntryService;

    /**
     * Endpoint para registrar marcaciones secuenciales (Entrada, Almuerzo Salida, Almuerzo Entrada, Salida).
     * POST /api/attendance/clock
     */
    @PostMapping("/clock")
    public ResponseEntity<?> registerClock(@RequestBody clockInRequest request) {
        try {
            timeEntry updatedEntry = timeEntryService.registerClock(request);
            return ResponseEntity.ok(updatedEntry);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la marcación: " + e.getMessage());
        }
    }

    /**
     * Endpoint para consultar las marcaciones de un usuario en una fecha específica.
     * GET /api/attendance/user/{userId}/date/{date} (Ejm: /api/attendance/user/1/date/2026-10-23)
     */
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<?> getByUserAndDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            timeEntry entry = timeEntryService.getByUserIdAndDate(userId, date);
            return ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            // En lugar de retornar 404 Not Found cuando no hay marcación hoy,
            // devolvemos 200 OK con un cuerpo nulo de forma limpia.
            return ResponseEntity.ok(null);
        }
    }

    /**
     * Endpoint para obtener todo el historial de marcaciones de un usuario.
     * GET /api/attendance/user/{userId}/history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getHistoryByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(timeEntryService.getHistoryByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
