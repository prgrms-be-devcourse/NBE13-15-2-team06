package com.kdt.fridge;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "fridge-management",
                version = "v1.0",
                description = "냉장고를 부탁해 API 문서입니다."
        )
)
public class FridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FridgeApplication.class, args);
    }

}
