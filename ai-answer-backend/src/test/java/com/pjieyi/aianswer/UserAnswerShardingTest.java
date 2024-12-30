package com.pjieyi.aianswer;

import com.pjieyi.aianswer.model.entity.UserAnswer;
import com.pjieyi.aianswer.service.UserAnswerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserAnswerShardingTest {

    @Resource
    private UserAnswerService userAnswerService;
    @Test
    void test(){
        UserAnswer userAnswer1=new UserAnswer();
        userAnswer1.setAppId(1L);
        userAnswer1.setUserId(1L);
        boolean save = userAnswerService.save(userAnswer1);
        UserAnswer userAnswer2=new UserAnswer();
        userAnswer2.setAppId(2L);
        userAnswer2.setUserId(2L);
        userAnswerService.save(userAnswer2);
    }
}
