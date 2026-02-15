package com.maxlvsh.eventtasks.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class FileEventLogger {

    private static final Logger log = LoggerFactory.getLogger(FileEventLogger.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path logFilePath;

    public FileEventLogger(@Value("${app.file.log.path}") String logFilePath) {
        this.logFilePath = Paths.get(logFilePath);
    }

    @PostConstruct
    public void init() {
        try {
            if (logFilePath.getParent() != null) {
                Files.createDirectories(logFilePath.getParent());
            }
            if (!Files.exists(logFilePath)) {
                Files.createFile(logFilePath);
                String header = "timestamp,eventId,externalId,status,message\n";
                Files.writeString(logFilePath, header, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            log.error("Failed to initialize log file: {}", e.getMessage());
        }
    }

    public void logEventProcessing(Long eventId, String externalId, String status, String message) {
        try {
            String logLine = String.format("%s,%d,%s,%s,%s\n",
                    LocalDateTime.now().format(FORMATTER),
                    eventId,
                    externalId,
                    status,
                    message.replace(",", ";")
            );

            Files.writeString(logFilePath, logLine, StandardOpenOption.APPEND);
            log.debug("Logged event processing to file: {}", eventId);

        } catch (IOException e) {
            log.error("Failed to write to log file: {}", e.getMessage());
        }
    }
}
