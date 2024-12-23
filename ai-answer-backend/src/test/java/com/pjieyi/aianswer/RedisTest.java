package com.pjieyi.aianswer;

import com.pjieyi.aianswer.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Test
    public void testRedis() {
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
//        valueOperations.set("yupi","你好");
        Object object = valueOperations.get("yupi");
        System.out.println(object);
    }

    @Test
    public void testHash(){
        User user=new User();
        user.setUserName("占山的撒发生范德萨分");
        user.setUserProfile("addafsafafaff");
//        redisTemplate.opsForHash().put("hash","key1",user);
//        Object object = redisTemplate.opsForHash().get("hash","key1");
//        System.out.println(object);
        boolean blank = StringUtils.isBlank("    ");
        System.out.println(blank);
    }
}
