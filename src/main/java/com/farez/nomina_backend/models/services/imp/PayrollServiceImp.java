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
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // 1. Rango dinámico según el año y mes consultados
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<timeEntry> entries = timeEntryRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(userId, startDate, endDate);

        long totalWorkedMinutes = 0;
        int totalLateMinutes = 0;
        double extraHours25 = 0.0;
        double extraHours100 = 0.0;

        // Registro de días únicos asistidos en el mes
        Set<LocalDate> workedDates = new HashSet<>();

        for (timeEntry entry : entries) {
            LocalDate date = entry.getWorkDate();
            boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);

            if (entry.getClockIn() != null && entry.getClockOut() != null) {
                workedDates.add(date);

                // Control de Atrasos (Lunes a Viernes)
                if (!isWeekend) {
                    LocalTime clockIn = entry.getClockIn().toLocalTime();
                    LocalTime limitTime = ENTRY_TIME.plusMinutes(TOLERANCE_MINUTES);

                    if (clockIn.isAfter(limitTime)) {
                        long lates = Duration.between(ENTRY_TIME, clockIn).toMinutes();
                        totalLateMinutes += lates;
                    }
                }

                // Horas Trabajadas
                long dayMinutes = Duration.between(entry.getClockIn(), entry.getClockOut()).toMinutes();

                // Descuento de almuerzo (si existe marcación o 1h por defecto de Lunes a Viernes)
                if (entry.getLunchOut() != null && entry.getLunchIn() != null) {
                    long lunchMinutes = Duration.between(entry.getLunchOut(), entry.getLunchIn()).toMinutes();
                    dayMinutes -= lunchMinutes;
                } else if (!isWeekend) {
                    dayMinutes -= 60;
                }

                dayMinutes = Math.max(0, dayMinutes);
                totalWorkedMinutes += dayMinutes;

                // Cálculo de Extras
                if (isWeekend) {
                    extraHours100 += (dayMinutes / 60.0);
                } else {
                    if (dayMinutes > 480) { // Más de 8 horas de trabajo
                        extraHours25 += ((dayMinutes - 480) / 60.0);
                    }
                }
            }
        }

        // 2. CÁLCULO DINÁMICO DE DÍAS LABORABLES Y FALTAS PARA CUALQUIER MES
        int expectedWorkingDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Contamos los días laborables esperados (Lunes a Sábado según la jornada de la empresa)
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                expectedWorkingDays++;
            }
        }

        // Días sin registro de asistencia en todo el mes
        int daysWorkedCount = workedDates.size();
        int missedDaysCount = Math.max(0, expectedWorkingDays - daysWorkedCount);

        // 3. CÁLCULOS MONETARIOS
        BigDecimal baseSalary = user.getBaseSalary() != null ? user.getBaseSalary() : BigDecimal.ZERO;

        // Tarifa por hora (Base 240 hrs) y Tarifa diaria (Base 30 días)
        BigDecimal hourlyRate = baseSalary.divide(new BigDecimal("240"), 4, RoundingMode.HALF_UP);
        BigDecimal dailyRate = baseSalary.divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP);

        // Descuento automático proporcional a los días faltados
        BigDecimal daysDeduction = dailyRate.multiply(new BigDecimal(missedDaysCount)).setScale(2, RoundingMode.HALF_UP);

        // Pago de Horas Extras
        BigDecimal pay25 = hourlyRate.multiply(new BigDecimal("1.25")).multiply(new BigDecimal(extraHours25));
        BigDecimal pay100 = hourlyRate.multiply(new BigDecimal("2.00")).multiply(new BigDecimal(extraHours100));
        BigDecimal overtimePay = pay25.add(pay100).setScale(2, RoundingMode.HALF_UP);

        // Totales de Liquidación
        BigDecimal totalIngresos = baseSalary.add(overtimePay);
        BigDecimal totalEgresos = ADVANCE_QUINCENA.add(daysDeduction);
        BigDecimal netSalary = totalIngresos.subtract(totalEgresos).setScale(2, RoundingMode.HALF_UP);

        return PayrollSummary.builder()
                .userId(user.getId())
                .employeeName(user.getFirstName() + " " + user.getLastName())
                .totalWorkedHours(Math.round((totalWorkedMinutes / 60.0) * 100.0) / 100.0)
                .totalLateMinutes(totalLateMinutes)
                .extraHours25(Math.round(extraHours25 * 100.0) / 100.0)
                .extraHours100(Math.round(extraHours100 * 100.0) / 100.0)
                .baseSalary(baseSalary)
                .advancePayment(ADVANCE_QUINCENA)
                .hourlyRate(hourlyRate.setScale(4, RoundingMode.HALF_UP))
                .overtimePay(overtimePay)
                .dayDeduction(daysDeduction)
                .netSalary(netSalary)
                .build();
    }
}