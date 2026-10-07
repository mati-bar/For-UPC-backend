package com.example.forupc_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ForUpcBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForUpcBackendApplication.class, args);
    }

}