//This is the main class for the Peti application.
//It is the entry point for the application.
//It is responsible for starting the application.
//It is also responsible for configuring the application.
//It is also responsible for starting the application.
//It is also responsible for configuring the application.

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