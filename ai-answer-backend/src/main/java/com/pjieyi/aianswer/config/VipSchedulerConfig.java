package com.pjieyi.aianswer.config;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义VIP线程池
 */
@Data
@Configuration
public class VipSchedulerConfig {

    @Bean
    public Scheduler vipScheduler(){
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber=new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t=new Thread(r,"VIPThreadPool-"+threadNumber.getAndIncrement());
                //设置为非守护线程  用于执行业务逻辑等核心任务，程序必须等待这些线程完成才能终止
                t.setDaemon(false);
                return t;
            }
        };
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10, threadFactory);
        return Schedulers.from(executorService);
    }
}
