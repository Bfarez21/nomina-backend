package com.farez.nomina_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollSummary {
    private Long userId;
    private String employeeName;

    //count horas -minutos
    private double totalWorkedHours;
    private int totalLateMinutes;
    private double extraHours50; //suplementaria
    private double extraHours100; //extraordinaria

    //valores
    private BigDecimal baseSalary;
    private BigDecimal advancePayment; //anticipo/quincena
    private BigDecimal hourlyRate; //hora normal
    private BigDecimal overtimePay; // total horas extras
    private BigDecimal lateDeduction; // descuento atraso
    private BigDecimal netSalary; // salario liquido a recibir
}
