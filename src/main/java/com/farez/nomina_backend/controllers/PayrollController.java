package com.farez.nomina_backend.controllers;

import com.farez.nomina_backend.dto.PayrollSummary;
import com.farez.nomina_backend.models.services.IPayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final IPayrollService payrollService;

    /**
     * Obtiene el resumen de nómina de un usuario por mes y año.
     * GET /api/payroll/user/{userId}?year=2026&month=10
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getMonthlyPayroll(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            PayrollSummary summary = payrollService.calculateMonthlyPayroll(userId, year, month);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
