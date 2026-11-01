package com.farez.nomina_backend.models.services.imp;

import com.farez.nomina_backend.dto.clockInRequest;
import com.farez.nomina_backend.models.dao.timeEntryRepository;
import com.farez.nomina_backend.models.dao.userRepository;
import com.farez.nomina_backend.models.entity.timeEntry;
import com.farez.nomina_backend.models.entity.User;
import com.farez.nomina_backend.models.services.ITimeEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class timeEntryServiceImp implements ITimeEntryService {

    @Autowired
    private  timeEntryRepository timeEntryRepository;
    @Autowired
    private  userRepository userRepository;

    // evita doble click
    private static final long COOLDOWN_MINUTES = 1;

    @Override
    @Transactional
    public timeEntry registerClock(clockInRequest request) {
        // 1. Determinar el timestamp (actual o personalizado para pruebas)
        LocalDateTime timestamp = request.getCustomTimestamp() != null
                ? request.getCustomTimestamp()
                : LocalDateTime.now();

        LocalDate workDate = timestamp.toLocalDate();

        // 2. Verificar que el usuario exista
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUserId()));

        // 3. Buscar si existe un registro del día o crear uno nuevo
        timeEntry entry = timeEntryRepository.findByUserIdAndWorkDate(user.getId(), workDate)
                .orElseGet(() -> timeEntry.builder()
                        .user(user)
                        .workDate(workDate)
                        .build());
    // validar doble click
        LocalDateTime lastClock = getLastClockTimestamp(entry);
        if (lastClock !=null){
            long minutesPassed = Duration.between(lastClock, timestamp).toMinutes();
            if (minutesPassed < COOLDOWN_MINUTES){
                throw new IllegalStateException("Ya realizaste el registro");
            }
        }

        // 5. Lógica secuencial para asignar la marca correspondiente
        if (entry.getClockIn() == null) {
            entry.setClockIn(timestamp);
        } else if (entry.getLunchOut() == null) {
            entry.setLunchOut(timestamp);
        } else if (entry.getLunchIn() == null) {
            entry.setLunchIn(timestamp);
        } else if (entry.getClockOut() == null) {
            entry.setClockOut(timestamp);
        } else {
            throw new IllegalStateException("El usuario ya registró las 4 marcaciones correspondientes al día de hoy.");
        }

        // 6. Guardar y retornar
        return timeEntryRepository.save(entry);
    }

    // Método auxiliar para obtener la última marcación registrada en el día
    private LocalDateTime getLastClockTimestamp(timeEntry entry) {
        if (entry.getClockOut() != null) return entry.getClockOut();
        if (entry.getLunchIn() != null) return entry.getLunchIn();
        if (entry.getLunchOut() != null) return entry.getLunchOut();
        if (entry.getClockIn() != null) return entry.getClockIn();
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public timeEntry getByUserIdAndDate(Long userId, LocalDate date) {
        if (!userRepository.existsById(userId)){
            throw new RuntimeException("El usuario con id " + userId + " no existe");
        }
        return timeEntryRepository.findByUserIdAndWorkDate(userId, date)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<timeEntry> getHistoryByUserId(Long userId) {
        //verificar existencia
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("El ususario con id " + userId+ " no existe");
        }
        //devuelve historia
        return timeEntryRepository.findByUserIdOrderByWorkDateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<timeEntry> getHistoryByUserIdAndMonth(Long userId, int year, int month) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("El usuario con id " + userId + " no existe");
        }
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return timeEntryRepository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(userId, startDate, endDate);
    }
}
