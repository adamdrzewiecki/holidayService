package com.adamdr.holidayservice.service;

import com.adamdr.holidayservice.dto.HolidayDto;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface HolidayInformationService {

    Mono<HolidayDto> getHolidayInformation(LocalDate date, String firstCountryCode, String secondCountryCode);
}
