package com.jigubangbang.chat_service.com_service;

import com.jigubangbang.chat_service.security.FeignClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComServiceClientConfig {

    // FeignClientInterceptor를 Spring 빈으로 등록
    // 이 설정 클래스는 UserServiceClient 인터페이스의 @FeignClient 어노테이션에 configuration 속성으로 지정
    // 예: @FeignClient(name = "user-service", configuration = UserServiceClientConfig.class)
    @Bean
    public FeignClientInterceptor comFeignClientInterceptor() {
        return new FeignClientInterceptor();
    }

}
