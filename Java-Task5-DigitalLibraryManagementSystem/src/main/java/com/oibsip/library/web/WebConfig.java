package com.oibsip.library.web;

import com.oibsip.library.model.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(User.Role.ADMIN))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new AuthInterceptor(User.Role.USER))
                .addPathPatterns("/user/**");
    }
}