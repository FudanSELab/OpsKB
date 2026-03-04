package com.xidian.kg;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class KgApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）- 10秒
        factory.setConnectTimeout(10000);
        // 设置读取超时时间（毫秒）- 5分钟，用于大文件上传
        factory.setReadTimeout(300000);
        // 设置缓冲区请求体
        factory.setBufferRequestBody(false);
        
        return new RestTemplate(factory);
    }
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 使用JsonMapper构建器，禁用非ASCII字符转义，使中文字符在日志中可读
        return JsonMapper.builder()
                .configure(JsonWriteFeature.ESCAPE_NON_ASCII, false)
                .build();
    }

}
