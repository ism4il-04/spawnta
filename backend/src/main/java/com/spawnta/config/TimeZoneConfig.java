package com.spawnta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

    @Value("${TIMEZONE:Europe/Paris}")
    private String timeZone;

    @PostConstruct
    public void init() {
        // Set default timezone for the JVM
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
        System.setProperty("user.timezone", timeZone);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        return mapper;
    }
}