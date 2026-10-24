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

        // 4. Lógica secuencial para asignar la marca correspondiente
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

        // 5. Guardar y retornar
        return timeEntryRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public timeEntry getByUserIdAndDate(Long userId, LocalDate date) {
        if (!userRepository.existsById(userId)){
            throw new RuntimeException("El usuario con id " + userId + " no existe");
        }
        return timeEntryRepository.findByUserIdAndWorkDate(userId, date)
                .orElseThrow(() -> new RuntimeException("No se encontraron marcaciones para el usuario " + userId + " en la fecha " + date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<timeEntry> getHistoryByUserId(Long userId) {
        //verificar existencia
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("El ususario con id " + userId+ " no existe");
        }
        //devuelve historia
        return timeEntryRepository.findAll().stream()
                .filter(entry -> entry.getUser().getId().equals(userId))
                .toList();
    }
}
