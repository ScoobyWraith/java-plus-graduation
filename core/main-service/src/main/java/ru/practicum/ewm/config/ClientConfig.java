package ru.practicum.ewm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final DiscoveryClient discoveryClient;

    @Bean
    public RestClient restClient(@Value("${discovery.stats-server-name}") String statsServerName) {
        ServiceInstance statsServer;

        try {
            statsServer = discoveryClient
                    .getInstances(statsServerName)
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с именем: " + statsServerName,
                    exception
            );
        }

        return RestClient.builder()
                .baseUrl(String.format("%s://%s:%s", statsServer.getScheme(), statsServer.getHost(), statsServer.getPort()))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper(@Value("${application.date-time-format}") String dateTimeFormat) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        ObjectMapper objectMapper = new ObjectMapper();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);

        javaTimeModule.addSerializer(new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
}
