package com.kdt.fridge.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FridgeOpenApiConfig {

    //Swagger UI 목록에 보일 그룹 v1생성
    @Bean
    public GroupedOpenApi v1Api(){
        return GroupedOpenApi.builder()
                .group("v1")
                //아래 주소에 해당하는 것만 이 그룹
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
