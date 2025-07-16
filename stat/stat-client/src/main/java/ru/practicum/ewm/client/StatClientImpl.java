package ru.practicum.ewm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatHitDto;
import ru.practicum.dto.StatViewDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatClientImpl implements StatClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper mapper;

    private RestClient restClient;

    @Value("${discovery.stats-server-name}")
    private String statsServiceName;

    public void hit(StatHitDto statHitDto) {
        setRestClientIfNeeded();
        String jsonBody;

        try {
            jsonBody = mapper.writeValueAsString(statHitDto);
            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Ошибка при записи hit", ex);
            throw new RuntimeException("Ошибка при записи hit: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<StatViewDto> getStat(LocalDateTime start, LocalDateTime end,
                                     List<String> uris, Boolean unique) {
        setRestClientIfNeeded();

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .path("/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", uris);
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String uri = builder.build().toUriString();

            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatViewDto>>() {
                    });
        } catch (RestClientException e) {
            log.error("Ошибка при запросе на получение статистики", e);
            return new ArrayList<>();
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceName)
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с именем: " + statsServiceName,
                    exception
            );
        }
    }

    private void setRestClientIfNeeded() {
        if (restClient != null) {
            return;
        }

        ServiceInstance statsServer = getInstance();

        restClient = RestClient.builder()
                .baseUrl(String.format("%s://%s:%s", statsServer.getScheme(), statsServer.getHost(), statsServer.getPort()))
                .build();
    }
}
