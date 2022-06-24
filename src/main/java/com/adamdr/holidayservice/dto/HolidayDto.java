package com.adamdr.holidayservice.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HolidayDto(LocalDate date, String name1, String name2) {}
