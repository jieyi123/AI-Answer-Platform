package com.pjieyi.aianswer;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;
    @Test
    void testLock(){
        RLock lock = redissonClient.getLock("testLock");
        try {
            System.out.println("开始执行");
            boolean res1 = lock.tryLock(10,10, TimeUnit.SECONDS);
            if (res1){
                Thread.sleep(15000);
                System.out.println("执行完毕");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
            System.out.println("锁关闭");
        }
    }
}
