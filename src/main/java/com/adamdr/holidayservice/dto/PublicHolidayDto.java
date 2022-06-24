package com.adamdr.holidayservice.dto;

import java.time.LocalDate;

public record PublicHolidayDto(LocalDate date, String localName, String name, String countryCode) {}
