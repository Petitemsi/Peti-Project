package com.peti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetiApplication.class, args);
    }
} 