package com.pjieyi.aianswer;

import cn.hutool.core.util.IdUtil;
import com.pjieyi.aianswer.model.dto.question.AiGenerateQuestionRequest;
import com.pjieyi.aianswer.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 主类测试
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private QuestionService questionService;

    @Test
    void test() {
        long snowflakeNextId = IdUtil.getSnowflakeNextId();
        System.out.println(snowflakeNextId);
    }

    @Test
    void testScheduler() throws InterruptedException {
        AiGenerateQuestionRequest questionRequest=new AiGenerateQuestionRequest();
        questionRequest.setAppId(3L);
        questionService.aiGenerateQuestionSseTest(questionRequest,false);
        questionService.aiGenerateQuestionSseTest(questionRequest,false);
        questionService.aiGenerateQuestionSseTest(questionRequest,true);
        Thread.sleep(1000000L);
    }
}
