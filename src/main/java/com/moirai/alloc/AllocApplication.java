package com.moirai.alloc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class AllocApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllocApplication.class, args);
    }

}
