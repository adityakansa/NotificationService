package com.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Entry Point for Notification System
 * 
 * This application demonstrates a scalable, extensible notification system
 * following SOLID principles and design patterns.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class NotificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationSystemApplication.class, args);
    }
}
