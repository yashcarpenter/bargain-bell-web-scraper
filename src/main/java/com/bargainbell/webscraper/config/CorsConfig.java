package com.bargainbell.webscraper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("*") // Allow all origins
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow these methods
        .allowedHeaders("*") // Allow all headers
        .allowCredentials(false) // Allow credentials (cookies, authorization headers, etc.)
        .maxAge(3600); // Cache the CORS configuration for 1 hour
  }
}
