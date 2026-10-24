package com.farez.nomina_backend.models.services;

import com.farez.nomina_backend.dto.clockInRequest;
import com.farez.nomina_backend.models.entity.timeEntry;

import java.time.LocalDate;
import java.util.List;

public interface ITimeEntryService {

    timeEntry registerClock(clockInRequest request); //registra las marcaciones del dia
    timeEntry getByUserIdAndDate(Long userId, LocalDate date); //consultaar marcaciones por fecha
    List<timeEntry> getHistoryByUserId(Long userId); // obtener historial marcasiones user
}
