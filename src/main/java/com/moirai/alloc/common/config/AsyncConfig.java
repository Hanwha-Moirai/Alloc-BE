package com.moirai.alloc.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 알림 SSE 이벤트 전용 Executor
     * - core 4 / max 8 / queue 1000 (필요 시 조정)
     * - 큐가 꽉 차면 CallerRunsPolicy 로 백프레셔(폭주 시 무한히 스레드 늘리는 것 방지)
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("notif-");
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(1000);
        exec.setKeepAliveSeconds(30);

        // 폭주 시 큐가 가득 차면 호출 스레드에서 실행(백프레셔)
        exec.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // (선택) MDC/Trace 전파가 필요하면 decorator 적용
        exec.setTaskDecorator(mdcTaskDecorator());

        exec.initialize();
        return exec;
    }

    private TaskDecorator mdcTaskDecorator() {
        return runnable -> runnable; // 필요하면 MDC 복사 구현
    }
}