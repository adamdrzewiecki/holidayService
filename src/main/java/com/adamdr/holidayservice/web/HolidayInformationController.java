package com.adamdr.holidayservice.web;

import com.adamdr.holidayservice.dto.HolidayDto;
import com.adamdr.holidayservice.service.HolidayInformationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestController
@RequestMapping(path = "/v1/holidays-information")
public class HolidayInformationController {

    private final HolidayInformationService holidayInformationService;

    @Autowired
    public HolidayInformationController(HolidayInformationService holidayInformationService) {
        this.holidayInformationService = holidayInformationService;
    }

    @Operation(summary = "Return next holiday after the given date in both countries", security = {@SecurityRequirement(name = "basicAuth")})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HolidayDto>> getNextHolidayForCountries(@RequestParam("date")
                                                                           @Parameter(description = "Date in format YYYY-MM-DD")
                                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = {"MM/dd/yyyy", "dd/MM/yyyy", "dd.MM.yyyy", "yyyy/MM/dd"})
                                                                           LocalDate date,
                                                                       @RequestParam("firstCountryCode")
                                                                           @Parameter(description = "First country code in ISO 3166-1 alpha-2 format")
                                                                           String firstCountryCode,
                                                                       @RequestParam("secondCountryCode")
                                                                           @Parameter(description = "Second country code in ISO 3166-1 alpha-2 format")
                                                                           String secondCountryCode) {
        return holidayInformationService.getHolidayInformation(date, firstCountryCode, secondCountryCode)
                .map(ResponseEntity::ok);
    }

    @ExceptionHandler({ TypeMismatchException.class, ServerWebInputException.class, NoSuchElementException.class})
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(Exception ex) {
        log.debug("Exception message: {}, exception cause: {}, exception stack trace: {}", ex.getMessage(), ex.getCause(), ex.getStackTrace());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect request param/params");
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<String> handleException(Exception ex) {
        log.debug("Exception message: {}, exception cause: {}, exception stack trace: {}", ex.getMessage(), ex.getCause(), ex.getStackTrace());
        return  ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
