package com.adamdr.holidayservice.web;

import com.adamdr.holidayservice.dto.HolidayDto;
import com.adamdr.holidayservice.service.HolidayInformationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {HolidayInformationController.class})
public class HolidayInformationControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @MockBean
    HolidayInformationService holidayInformationService;

    private final String holidayResource = "/v1/holidays-information";

    @Test
    public void shouldReturnUnauthorized() {
        //then
        webTestClient
                .get()
                .uri(holidayResource)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForNoneParams() {
        //then
        webTestClient
                .get()
                .uri(holidayResource)
                .header("Authorization", "someValue")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForUnhandledDateFormat() {
        //then
        webTestClient
                .get()
                .uri(holidayResource + "?date=2022-MAY-21&firstCountryCode=PL&secondCountryCode=GB")
                .header("Authorization", "someValue")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForNullDate() {
        //then
        webTestClient
                .get()
                .uri(holidayResource + "?date=firstCountryCode=PL&secondCountryCode=GB")
                .header("Authorization", "someValue")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldGetHolidayInformationForGivenParams() {
        //given
        LocalDate date = LocalDate.of(2022, 1, 3);
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        when(holidayInformationService.getHolidayInformation(any(LocalDate.class), anyString(), anyString()))
                .thenReturn(Mono.just(new HolidayDto(LocalDate.of(2022, 4, 18), "Drugi Dzień Wielkanocy", "Easter Monday")));
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(HolidayDto.class)
                .value(holidayDto -> {
                    assertThat(holidayDto.date()).isAfterOrEqualTo(date);
                    assertThat(holidayDto.name1()).isEqualTo("Drugi Dzień Wielkanocy");
                    assertThat(holidayDto.name2()).isEqualTo("Easter Monday");
                });
    }
}
