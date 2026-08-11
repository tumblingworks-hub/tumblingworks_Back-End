package com.tumblingworks.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI tumblingworksOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Tumblingworks Back-End API")
						.description("Tumblingworks Back-End REST API documentation")
						.version("v1"));
	}
}
