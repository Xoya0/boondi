package com.boondi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BoondiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoondiApplication.class, args);
    }
}
