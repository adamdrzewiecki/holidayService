package com.adamdr.holidayservice.web;

import com.adamdr.holidayservice.dto.HolidayDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class HolidayInformationControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    private final String holidayResource = "/v1/holidays-information";

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForNullDate() {
        //given
        LocalDate date = null;
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForNullCountryCode() {
        //given
        LocalDate date = LocalDate.of(2022, 1, 3);
        String firstCountryCode = "PL";
        String secondCountryCode = null;
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForIncorrectDateFormat() {
        //given
        String date = "2022-MAY-12";
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForIncorrectCountryCode() {
        //given
        String date = "2022-05-12";
        String firstCountryCode = "POL";
        String secondCountryCode = "GB";
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForADateInTheDistantPast() {
        //given
        String date = "1525-04-10";
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser
    public void shouldReturnBadRequestForADateInTheFarFuture() {
        //given
        String date = "2125-04-10";
        String firstCountryCode = "PL";
        String secondCountryCode = "GB";
        //then
        webTestClient
                .get()
                .uri(String.format(holidayResource + "?date=%s&firstCountryCode=%s&secondCountryCode=%s", date, firstCountryCode, secondCountryCode))
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

    @Test
    @WithMockUser
    public void shouldGetHolidayInformationForGivenParamsInSwitchedOrder() {
        //given
        LocalDate date = LocalDate.of(2022, 1, 3);
        String firstCountryCode = "GB";
        String secondCountryCode = "PL";
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
                    assertThat(holidayDto.name1()).isEqualTo("Easter Monday");
                    assertThat(holidayDto.name2()).isEqualTo("Drugi Dzień Wielkanocy");
                });
    }
}
