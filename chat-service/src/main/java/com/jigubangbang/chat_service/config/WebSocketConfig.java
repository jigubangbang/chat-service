package com.jigubangbang.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
    ts.setPoolSize(1);
    ts.setThreadNamePrefix("wss-heartbeat-thread-");
    ts.initialize();

    registry.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{30000, 30000})
            .setTaskScheduler(ts);
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*");
            //.setAllowedOrigins("http://localhost:8080") // CORS 설정
            //.withSockJS().setSuppressCors(true);          // SockJS fallback 사용
  }

}
