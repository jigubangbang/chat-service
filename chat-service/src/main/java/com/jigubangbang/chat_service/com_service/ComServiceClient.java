package com.jigubangbang.chat_service.com_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient( name="com-service", configuration = ComServiceClientConfig.class )
public interface ComServiceClient {  
}
