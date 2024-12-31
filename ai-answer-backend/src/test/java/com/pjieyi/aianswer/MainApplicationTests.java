package com.pjieyi.aianswer;

import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 */
@SpringBootTest
class MainApplicationTests {


    @Test
    void test() {
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        System.out.println(snowflakeNextId);
    }
}
