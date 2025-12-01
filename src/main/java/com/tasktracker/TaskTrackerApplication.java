package com.tasktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Task Tracker API.
 * This is the entry point of the application.
 */
@SpringBootApplication
public class TaskTrackerApplication {
    
    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskTrackerApplication.class, args);
    }
}

