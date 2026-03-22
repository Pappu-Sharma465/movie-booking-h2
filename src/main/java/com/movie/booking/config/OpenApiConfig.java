package com.movie.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Movie Booking Platform API")
                .description(
                    "REST API for the Movie Ticket Booking Platform.\n\n" +
                    "**B2B endpoints** (`/api/b2b/`) — for registered theatre partners:\n" +
                    "onboard theatres, manage shows, control seat inventory.\n\n" +
                    "**B2C endpoints** (`/api/`) — for end customers:\n" +
                    "browse shows, view seat maps, book and cancel tickets.\n\n" +
                    "**Offers applied automatically at booking time:**\n" +
                    "50% off every 3rd ticket | 20% off all afternoon shows (12:00-17:00).")
            )
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")
            ));
    }
}
