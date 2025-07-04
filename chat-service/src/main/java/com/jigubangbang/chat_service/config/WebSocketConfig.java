package com.jigubangbang.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");     // -> 브로커(SimpleBroker)
    registry.setApplicationDestinationPrefixes("/app");   // -> 서버(SimpAnnotationMethod) -> broker(Channel) -> /topic -> 브로커(SimpleBroker)
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*");
            //.setAllowedOrigins("http://localhost:8080") // CORS 설정
            //.withSockJS().setSuppressCors(true);          // SockJS fallback 사용
  }

}
