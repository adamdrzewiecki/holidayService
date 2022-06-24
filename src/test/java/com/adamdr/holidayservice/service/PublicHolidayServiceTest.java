package com.adamdr.holidayservice.service;

import com.adamdr.holidayservice.dto.PublicHolidayDto;
import com.adamdr.holidayservice.http.WebClientInstance;
import com.adamdr.holidayservice.service.impl.PublicHolidayServiceImpl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest
@ContextConfiguration(classes = {PublicHolidayServiceImpl.class, WebClientInstance.class})
public class PublicHolidayServiceTest {

    @Autowired
    PublicHolidayService publicHolidayService;
    @Autowired
    WebClientInstance webClientInstance;
    static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("holidaysApiUrl", () -> "http://localhost:" + mockWebServer.getPort() + "/");
    }

    @Test
    public void shouldThrowNoSuchElementExceptionOn400StatusCodeFromHolidayApi() {
        //given
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        //when
        Flux<PublicHolidayDto> publicHolidayDtoFlux = publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(LocalDate.of(2022, 1, 1), "PL");
        //then
        StepVerifier
                .create(publicHolidayDtoFlux)
                .expectError(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowRuntimeExceptionOn500StatusCodeFromHolidayApi() {
        //given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        //when
        Flux<PublicHolidayDto> publicHolidayDtoFlux = publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(LocalDate.of(2022, 1, 1), "PL");
        //then
        StepVerifier
                .create(publicHolidayDtoFlux)
                .expectError(RuntimeException.class);
    }

    @Nested
    class shouldFilterHolidaysBeforeGivenDateClass {
        @Test
        public void shouldFilterHolidaysBeforeGivenDate() {
            //given
            LocalDate date = LocalDate.of(2023, 1, 3);
            final Dispatcher dispatcher = new Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                    return switch (Objects.requireNonNull(recordedRequest.getPath())) {
                        case "/2023/PL" -> new MockResponse().setResponseCode(200)
                                .setHeader("Content-Type", "application/octet-stream")
                                .setBody(
                                        "[\n" +
                                                "  {\n" +
                                                "    \"date\": \"2023-01-01\",\n" +
                                                "    \"localName\": \"Nowy Rok\",\n" +
                                                "    \"name\": \"New Year's Day\",\n" +
                                                "    \"countryCode\": \"PL\"\n" +
                                                "  },\n" +
                                                "  {\n" +
                                                "    \"date\": \"2023-01-06\",\n" +
                                                "    \"localName\": \"Święto Trzech Króli\",\n" +
                                                "    \"name\": \"Epiphany\",\n" +
                                                "    \"countryCode\": \"PL\"\n" +
                                                "  }\n" +
                                                "]");
                        default -> new MockResponse().setResponseCode(404);
                    };
                }
            };
            mockWebServer.setDispatcher(dispatcher);
            //when
            Flux<PublicHolidayDto> publicHolidayDtoFlux = publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(date, "PL");
            //then
            StepVerifier
                    .create(publicHolidayDtoFlux)
                    .consumeNextWith(publicHolidayDto -> assertThat(publicHolidayDto.date()).isAfter(date))
                    .verifyComplete();
        }
    }


    @Nested
    class shouldRetryOnEmptyResponseFromHolidayApiClass {
        @Test
        public void shouldRetryOnEmptyResponseFromHolidayApi() {
            //given
            final Dispatcher dispatcher = new Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                    return switch (Objects.requireNonNull(recordedRequest.getPath())) {
                        case "/2022/PL" -> new MockResponse().setResponseCode(200);
                        case "/2023/PL" -> new MockResponse().setResponseCode(200)
                                .setHeader("Content-Type", "application/octet-stream")
                                .setBody(
                                        "[\n" +
                                                "  {\n" +
                                                "    \"date\": \"2023-01-01\",\n" +
                                                "    \"localName\": \"Nowy Rok\",\n" +
                                                "    \"name\": \"New Year's Day\",\n" +
                                                "    \"countryCode\": \"PL\"\n" +
                                                "  },\n" +
                                                "  {\n" +
                                                "    \"date\": \"2023-01-06\",\n" +
                                                "    \"localName\": \"Święto Trzech Króli\",\n" +
                                                "    \"name\": \"Epiphany\",\n" +
                                                "    \"countryCode\": \"PL\"\n" +
                                                "  }\n" +
                                                "]");
                        default -> new MockResponse().setResponseCode(404);
                    };
                }
            };
            mockWebServer.setDispatcher(dispatcher);
            //when
            Flux<PublicHolidayDto> publicHolidayDtoFlux = publicHolidayService.getPublicHolidaysForCountryAfterGivenDate(LocalDate.of(2022, 12, 31), "PL");
            //then
            StepVerifier
                    .create(publicHolidayDtoFlux)
                    .consumeNextWith(publicHolidayDto -> assertThat(publicHolidayDto).isNotNull())
                    .consumeNextWith(publicHolidayDto -> assertThat(publicHolidayDto).isNotNull())
                    .verifyComplete();
        }
    }
}
