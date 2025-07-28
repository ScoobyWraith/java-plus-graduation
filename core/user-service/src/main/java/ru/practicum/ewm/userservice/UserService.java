package ru.practicum.ewm.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"ru.practicum.ewm.common.interaction"})
@SpringBootApplication
public class UserService {
    public static void main(String[] args) {
        SpringApplication.run(UserService.class, args);
    }
}