package ru.practicum.infra.discoveryserver;

@SpringBootApplication
@EnableEurekaServer
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}