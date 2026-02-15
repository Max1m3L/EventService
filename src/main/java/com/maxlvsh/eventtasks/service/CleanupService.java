package com.maxlvsh.eventtasks.service;

import com.maxlvsh.eventtasks.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
@EnableScheduling
public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final EventRepository eventRepository;
    private final Path logFilePath;
    private final int retentionDays;

    public CleanupService(EventRepository eventRepository,
                          @Value("${app.file.log.path}") String logFilePath,
                          @Value("${app.event.retention-days}") int retentionDays) {
        this.eventRepository = eventRepository;
        this.logFilePath = Paths.get(logFilePath);
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupDatabase() {
        log.info("Starting database cleanup for records older than {} days", retentionDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        try {
            Integer deletedCount = eventRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Database cleanup completed. Deleted {} records", deletedCount);
        } catch (Exception e) {
            log.error("Error during database cleanup: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupLogFile() {
        log.info("Starting log file cleanup for entries older than {} days", retentionDays);

        try {
            if (!Files.exists(logFilePath)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            Path tempFile = Paths.get(logFilePath.toString() + ".tmp");

            try (Stream<String> lines = Files.lines(logFilePath)) {
                Files.write(tempFile,
                        lines.skip(1)
                                .filter(line -> isLineRecent(line, cutoffDate))
                                .toList()
                );
            }

            Files.delete(logFilePath);
            Files.move(tempFile, logFilePath);

            if (Files.size(logFilePath) > 0) {
                String content = Files.readString(logFilePath);
                Files.writeString(logFilePath, "timestamp,eventId,externalId,status,message\n" + content);
            }

            log.info("Log file cleanup completed");

        } catch (IOException e) {
            log.error("Error during log file cleanup: {}", e.getMessage());
        }
    }

    private boolean isLineRecent(String line, LocalDateTime cutoffDate) {
        try {
            String timestampStr = line.split(",")[0];
            LocalDateTime lineDate = LocalDateTime.parse(timestampStr, FORMATTER);
            return lineDate.isAfter(cutoffDate);
        } catch (Exception e) {
            log.warn("Failed to parse log line, keeping it: {}", line);
            return true;
        }
    }
}
