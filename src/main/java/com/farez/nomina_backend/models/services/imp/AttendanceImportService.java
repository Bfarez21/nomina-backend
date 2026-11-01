package com.farez.nomina_backend.models.services.imp;

import com.farez.nomina_backend.models.dao.timeEntryRepository;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.User;
import com.farez.nomina_backend.models.entity.timeEntry;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceImportService {

    private final timeEntryRepository timeEntryRepo;
    private final userRepository userRepo;

    @Transactional
    public int importAttendanceFromExcel(MultipartFile file, Long userId, int year, int month) throws Exception {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        int importedCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 5; // Fila 6 del Excel (primera fila de datos)

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell dayCell = row.getCell(0);
                if (dayCell == null || dayCell.getCellType() == CellType.BLANK) continue;

                int day;
                if (dayCell.getCellType() == CellType.NUMERIC) {
                    day = (int) dayCell.getNumericCellValue();
                } else {
                    try {
                        day = Integer.parseInt(dayCell.getStringCellValue().trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if (day < 1 || day > 31) continue;

                LocalDate workDate = LocalDate.of(year, month, day);

                LocalTime clockIn = parseTimeCell(row.getCell(1));
                LocalTime lunchOut = parseTimeCell(row.getCell(2));
                LocalTime lunchIn = parseTimeCell(row.getCell(3));
                LocalTime clockOut = parseTimeCell(row.getCell(4));

                if (clockIn == null) continue;

                Optional<timeEntry> existingOpt = timeEntryRepo.findByUserIdAndWorkDate(userId, workDate);
                timeEntry entry = existingOpt.orElse(new timeEntry());

                entry.setUser(user);
                entry.setWorkDate(workDate);
                entry.setClockIn(LocalDateTime.of(workDate, clockIn));
                entry.setLunchOut(lunchOut != null ? LocalDateTime.of(workDate, lunchOut) : null);
                entry.setLunchIn(lunchIn != null ? LocalDateTime.of(workDate, lunchIn) : null);
                entry.setClockOut(clockOut != null ? LocalDateTime.of(workDate, clockOut) : null);

                timeEntryRepo.save(entry);
                importedCount++;
            }
        }

        return importedCount;
    }

    private LocalTime parseTimeCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalTime();
                }
                double value = cell.getNumericCellValue();
                int totalSeconds = (int) Math.round(value * 86400);
                int hours = (totalSeconds / 3600) % 24;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;
                return LocalTime.of(hours, minutes, seconds);
            }
            if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty() || val.equals("--:--")) return null;
                if (val.length() == 4) val = "0" + val;
                return LocalTime.parse(val, DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}