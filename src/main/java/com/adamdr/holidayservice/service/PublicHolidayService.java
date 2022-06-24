package com.adamdr.holidayservice.service;

import com.adamdr.holidayservice.dto.PublicHolidayDto;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface PublicHolidayService {

    Flux<PublicHolidayDto> getPublicHolidaysForCountryAfterGivenDate(LocalDate date, String countryCode);
}
