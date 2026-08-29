package com.tumblingworks.backend.config;

import com.tumblingworks.backend.interceptor.RequestLifecycleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	private final RequestLifecycleInterceptor requestLifecycleInterceptor;

	public InterceptorConfig(
			RequestLifecycleInterceptor requestLifecycleInterceptor
	) {
		this.requestLifecycleInterceptor = requestLifecycleInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestLifecycleInterceptor)
				.addPathPatterns("/api/**");
	}
}
