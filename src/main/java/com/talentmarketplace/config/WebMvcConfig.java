package com.talentmarketplace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/login/").setViewName("forward:/login.html");
        registry.addViewController("/login/project-manager").setViewName("forward:/login-pm.html");
        registry.addViewController("/login/project-manager/").setViewName("forward:/login-pm.html");
        registry.addViewController("/login/resource-manager").setViewName("forward:/login-rm.html");
        registry.addViewController("/login/resource-manager/").setViewName("forward:/login-rm.html");
    }
}
