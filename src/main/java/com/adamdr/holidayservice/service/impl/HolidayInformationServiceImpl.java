package com.adamdr.holidayservice.service.impl;

import com.adamdr.holidayservice.dto.HolidayDto;
import com.adamdr.holidayservice.dto.PublicHolidayDto;
import com.adamdr.holidayservice.service.HolidayInformationService;
import com.adamdr.holidayservice.service.PublicHolidayService;
import com.adamdr.holidayservice.utils.IsoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HolidayInformationServiceImpl implements HolidayInformationService {

    private final PublicHolidayService publicHolidayService;

    @Autowired
    public HolidayInformationServiceImpl(PublicHolidayService publicHolidayService) {
        this.publicHolidayService = publicHolidayService;
    }

    @Override
    public Mono<HolidayDto> getHolidayInformation(LocalDate date, String firstCountryCode, String secondCountryCode) {
        if (IsoUtils.isValidISOCountry(firstCountryCode) && IsoUtils.isValidISOCountry(secondCountryCode)) {
            return getPublicHolidaysList(date, firstCountryCode, secondCountryCode)
                    .map(this::getHolidaysFromBothCountriesAsMapGroupedByDate)
                    .switchIfEmpty(Mono.error(new NoSuchElementException("Can't find any holiday for both countries")))
                    .map(publicHolidayMap -> buildHolidayDto(firstCountryCode, secondCountryCode, publicHolidayMap));
        } else
            return Mono.error(() -> new NoSuchElementException("Country codes validation failed"));
    }

    private Mono<Map<LocalDate, List<PublicHolidayDto>>> getPublicHolidaysList(LocalDate date, String firstCountryCode, String secondCountryCode) {
        return Flux.just(firstCountryCode, secondCountryCode)
                .flatMap(countryCode -> publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(date, countryCode))
                .collect(Collectors.groupingBy(PublicHolidayDto::date));
    }

    private Map<String, PublicHolidayDto> getHolidaysFromBothCountriesAsMapGroupedByDate(Map<LocalDate, List<PublicHolidayDto>> localDateListMap) {
        return localDateListMap.entrySet().stream()
                .filter(localDateListEntry -> localDateListEntry.getValue().size() == 2)
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(publicHolidayDtos -> publicHolidayDtos.stream().collect(Collectors.toMap(PublicHolidayDto::countryCode, publicHolidayDto -> publicHolidayDto)))
                .orElseThrow(() -> new NoSuchElementException("Empty response from Public Holiday API"));
    }

    private HolidayDto buildHolidayDto(String firstCountryCode, String secondCountryCode, Map<String, PublicHolidayDto> publicHolidayMap) {
        return HolidayDto.builder()
                .date(publicHolidayMap.get(firstCountryCode).date())
                .name1(publicHolidayMap.get(firstCountryCode).localName())
                .name2(publicHolidayMap.get(secondCountryCode).localName())
                .build();
    }
}
