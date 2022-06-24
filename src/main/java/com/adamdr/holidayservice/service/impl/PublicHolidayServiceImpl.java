package com.adamdr.holidayservice.service.impl;

import com.adamdr.holidayservice.dto.PublicHolidayDto;
import com.adamdr.holidayservice.http.WebClientInstance;
import com.adamdr.holidayservice.service.PublicHolidayService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class PublicHolidayServiceImpl implements PublicHolidayService {

    private final WebClientInstance webClientInstance;
    private final String holidaysApiUrl;

    @Autowired
    public PublicHolidayServiceImpl(WebClientInstance webClientInstance, @Value("${holidaysApiUrl}") String holidaysApiUrl) {
        this.webClientInstance = webClientInstance;
        this.holidaysApiUrl = holidaysApiUrl;
    }

    @Override
    public Flux<PublicHolidayDto> getPublicHolidaysForCountryAfterGivenDate(LocalDate date, String countryCode) {
        return getPublicHolidays(date, countryCode)
                .filter(publicHolidayDto -> publicHolidayDto.date().isAfter(date))
                .switchIfEmpty(Flux.defer(() -> getPublicHolidays(date.plusYears(1l).withMonth(1).withDayOfMonth(1), countryCode)));
    }

    private Flux<PublicHolidayDto> getPublicHolidays(LocalDate date, String countryCode) {
        return webClientInstance.getWebClient()
                .get()
                .uri(holidaysApiUrl + date.getYear() + "/" + countryCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new NoSuchElementException("Response from Public Holiday API with status code = " + clientResponse.statusCode().value())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Response from Public Holiday API with status code = " + clientResponse.statusCode().value())))
                .bodyToFlux(PublicHolidayDto.class);
    }
}
