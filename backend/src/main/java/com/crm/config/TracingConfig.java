package com.crm.config;

import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configura RestClient con soporte automático de propagación W3C traceparent.
 * Micrometer Tracing + OTEL bridge inyectan el header automáticamente
 * cuando se usa el ClientHttpRequestInterceptor de Micrometer.
 *
 * Para Dynatrace: el agente DT también intercepta RestClient/WebClient
 * y añade x-dynatrace si está presente.
 */
@Configuration
public class TracingConfig {

    /**
     * RestClient con interceptores de Micrometer para propagación de contexto.
     * Inyectá este bean donde necesites llamar a servicios externos.
     */
    @Bean
    public RestClient tracedRestClient(RestClient.Builder builder) {
        // Spring Boot 3.3 auto-configura el builder con
        // ObservationRestClientCustomizer que añade traceparent automáticamente.
        return builder.build();
    }
}
