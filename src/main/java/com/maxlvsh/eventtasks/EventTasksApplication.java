package com.maxlvsh.eventtasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventTasksApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventTasksApplication.class, args);
    }

}
