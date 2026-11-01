package com.farez.nomina_backend.models.services.imp;

import com.farez.nomina_backend.dto.PayrollSummary;
import com.farez.nomina_backend.models.dao.timeEntryRepository;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.User;
import com.farez.nomina_backend.models.entity.timeEntry;
import com.farez.nomina_backend.models.services.IPayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollServiceImp implements IPayrollService {

    private final timeEntryRepository timeEntryRepository;
    private final userRepository userRepository;

    private static final LocalTime ENTRY_TIME = LocalTime.of(8, 0);
    private static final int TOLERANCE_MINUTES = 10;
    private static final BigDecimal ADVANCE_QUINCENA = new BigDecimal("100.00");

    @Override
    @Transactional(readOnly = true)
    public PayrollSummary calculateMonthlyPayroll(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<timeEntry> entries = timeEntryRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(userId, startDate, endDate);

        long totalWorkedMinutes = 0;
        int totalLateMinutes = 0;
        double extraHours50 = 0.0;
        double extraHours100 = 0.0;

        for (timeEntry entry : entries) {
            LocalDate date = entry.getWorkDate();
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);

            // 1. Atrasos (Solo Lunes a Viernes)
            if (!isWeekend && entry.getClockIn() != null) {
                LocalTime clockIn = entry.getClockIn().toLocalTime();
                LocalTime limitTime = ENTRY_TIME.plusMinutes(TOLERANCE_MINUTES);

                if (clockIn.isAfter(limitTime)) {
                    long lates = Duration.between(ENTRY_TIME, clockIn).toMinutes();
                    totalLateMinutes += lates;
                }
            }

            // 2. Horas Trabajadas y Extras
            if (entry.getClockIn() != null && entry.getClockOut() != null) {
                long dayMinutes = Duration.between(entry.getClockIn(), entry.getClockOut()).toMinutes();

                // Descuento de 1 hora de almuerzo
                if (entry.getLunchOut() != null && entry.getLunchIn() != null) {
                    long lunchMinutes = Duration.between(entry.getLunchOut(), entry.getLunchIn()).toMinutes();
                    dayMinutes -= lunchMinutes;
                } else {
                    dayMinutes -= 60; // Descuento estándar 1h
                }

                dayMinutes = Math.max(0, dayMinutes);
                totalWorkedMinutes += dayMinutes;

                // Cálculo de Extras
                if (isWeekend) {
                    extraHours100 += (dayMinutes / 60.0);
                } else {
                    if (dayMinutes > 480) { // Más de 8 horas en día normal
                        extraHours50 += ((dayMinutes - 480) / 60.0);
                    }
                }
            }
        }

        // 3. Cálculos Monetarios
        BigDecimal baseSalary = user.getBaseSalary() != null ? user.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal hourlyRate = baseSalary.divide(new BigDecimal("160"), 4, RoundingMode.HALF_UP);
        BigDecimal minuteRate = hourlyRate.divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        BigDecimal lateDeduction = minuteRate.multiply(new BigDecimal(totalLateMinutes)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal pay50 = hourlyRate.multiply(new BigDecimal("1.50")).multiply(new BigDecimal(extraHours50));
        BigDecimal pay100 = hourlyRate.multiply(new BigDecimal("2.00")).multiply(new BigDecimal(extraHours100));
        BigDecimal overtimePay = pay50.add(pay100).setScale(2, RoundingMode.HALF_UP);

        BigDecimal netSalary = baseSalary.add(overtimePay).subtract(lateDeduction).subtract(ADVANCE_QUINCENA).setScale(2, RoundingMode.HALF_UP);

        return PayrollSummary.builder()
                .userId(user.getId())
                .employeeName(user.getFirstName() + " " + user.getLastName())
                .totalWorkedHours(Math.round((totalWorkedMinutes / 60.0) * 100.0) / 100.0)
                .totalLateMinutes(totalLateMinutes)
                .extraHours50(Math.round(extraHours50 * 100.0) / 100.0)
                .extraHours100(Math.round(extraHours100 * 100.0) / 100.0)
                .baseSalary(baseSalary)
                .advancePayment(ADVANCE_QUINCENA)
                .hourlyRate(hourlyRate.setScale(2, RoundingMode.HALF_UP))
                .overtimePay(overtimePay)
                .lateDeduction(lateDeduction)
                .netSalary(netSalary)
                .build();
    }
}
