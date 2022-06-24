package com.adamdr.holidayservice.service;

import com.adamdr.holidayservice.dto.HolidayDto;
import com.adamdr.holidayservice.dto.PublicHolidayDto;
import com.adamdr.holidayservice.service.impl.HolidayInformationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {HolidayInformationServiceImpl.class})
public class HolidayInformationServiceTest {

    @Autowired
    HolidayInformationService holidayInformationService;
    @MockBean
    PublicHolidayService publicHolidayService;

    @Test
    public void shouldThrowNoSuchElementExceptionAtCountryCodeValidation() {
        //given
        LocalDate date = LocalDate.of(2022, 1, 5);
        String firstCountryCode = "POL";
        String secondCountryCode = "ENG";
        //when
        Mono<HolidayDto> holidayDtoMono = holidayInformationService.getHolidayInformation(date, firstCountryCode, secondCountryCode);
        //then
        StepVerifier
                .create(holidayDtoMono)
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    public void shouldThrowNoSuchElementExceptionForEmptyResponseFromPublicHolidayService() {
        //given
        LocalDate date = LocalDate.of(2022, 1, 5);
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        when(publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(any(LocalDate.class), anyString())).thenReturn(Flux.empty());
        //when
        Mono<HolidayDto> holidayDtoMono = holidayInformationService.getHolidayInformation(date, firstCountryCode, secondCountryCode);
        //then
        StepVerifier
                .create(holidayDtoMono)
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    public void shouldThrowNoSuchElementExceptionForNoMatchingHolidays() {
        //given
        AtomicInteger counter = new AtomicInteger(1);
        LocalDate date = LocalDate.of(2022, 12, 27);
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        when(publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(any(LocalDate.class), anyString()))
                .thenAnswer(invocation -> Flux.just(
                        new PublicHolidayDto(invocation.getArgument(0, LocalDate.class).plusMonths(counter.getAndIncrement() + 3L).plusDays(counter.getAndIncrement() * 7L),
                                invocation.getArgument(1), invocation.getArgument(1), invocation.getArgument(1)),
                        new PublicHolidayDto(invocation.getArgument(0, LocalDate.class).plusMonths(counter.getAndIncrement() + 5L).plusDays(counter.getAndIncrement() * 9L),
                                invocation.getArgument(1), invocation.getArgument(1), invocation.getArgument(1))
                ));
        //when
        Mono<HolidayDto> holidayDtoMono = holidayInformationService.getHolidayInformation(date, firstCountryCode, secondCountryCode);
        //then
        StepVerifier
                .create(holidayDtoMono)
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    public void shouldReturnHolidayDto() {
        //given
        AtomicInteger localNameListCounter = new AtomicInteger(0);
        List<String> localNameList = List.of("Drugi Dzień Wielkanocy", "Easter Monday");
        LocalDate date = LocalDate.of(2022, 1, 5);
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        when(publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(any(LocalDate.class), anyString()))
                .thenAnswer(invocation -> Flux.just(
                        new PublicHolidayDto(LocalDate.of(2022, 4, 18), localNameList.get(localNameListCounter.getAndIncrement()), "Easter Monday", invocation.getArgument(1))
                ));
        //when
        Mono<HolidayDto> holidayDtoMono = holidayInformationService.getHolidayInformation(date, firstCountryCode, secondCountryCode);
        //then
        StepVerifier
                .create(holidayDtoMono)
                .consumeNextWith(holidayDto -> {
                    assertThat(holidayDto.date()).isAfter(date);
                    assertThat(holidayDto.name1()).isNotBlank();
                    assertThat(holidayDto.name2()).isNotBlank();
                })
                .verifyComplete();
    }
}
