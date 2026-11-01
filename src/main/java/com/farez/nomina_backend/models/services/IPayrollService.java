package com.farez.nomina_backend.models.services;

import com.farez.nomina_backend.dto.PayrollSummary;

public interface IPayrollService {
    PayrollSummary calculateMonthlyPayroll(Long userId, int year, int month);
}
