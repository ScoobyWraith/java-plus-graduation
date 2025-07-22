package ru.practicum.ewm.requestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
public class RequestService {
    public static void main(String[] args) {
        SpringApplication.run(RequestService.class);
    }
}