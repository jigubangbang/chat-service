// package com.jigubangbang.chat_service.websocket;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class CorsConfig implements WebMvcConfigurer {

//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**") // 모든 엔드포인트에 적용
//                 .allowedOrigins("http://localhost:5173") // React 클라이언트 오리진
//                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용 메서드
//                 .allowedHeaders("*") // 모든 헤더 허용
//                 .allowCredentials(true); // 인증 정보 허용
//     }
// }