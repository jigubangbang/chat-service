package com.jigubangbang.chat_service.com_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jigubangbang.chat_service.model.TravelmateDto;
import com.jigubangbang.chat_service.model.TravelInfoDto;

@FeignClient( name="com-service", configuration = ComServiceClientConfig.class )
public interface ComServiceClient {
    @GetMapping( "/user/{userId}" )
    TravelmateDto getMateDes( @PathVariable("userId") String userId );   
}
