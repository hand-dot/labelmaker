package com.hand.dot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class LabelmakerApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(LabelmakerApplication.class, args);
	}
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LabelmakerApplication.class);
    }
}
