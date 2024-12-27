package com.pjieyi.aianswer.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    private String secretKey;

    @Bean
    public ClientV4 getClient() {
        return new ClientV4.Builder(secretKey).build();
    }
}
