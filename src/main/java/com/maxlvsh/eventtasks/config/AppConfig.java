package com.maxlvsh.eventtasks.config;

import com.maxlvsh.eventtasks.entity.EventEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class AppConfig {

    @Value("${app.event.queue.capacity:1000}")
    private int queueCapacity;

    @Bean
    public BlockingQueue<EventEntity> eventQueue() {
        return new LinkedBlockingQueue<>(queueCapacity);
    }

    @Bean(name = "consumerTaskExecutor")
    public Executor consumerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("EventConsumer-");
        executor.initialize();
        return executor;
    }
}
