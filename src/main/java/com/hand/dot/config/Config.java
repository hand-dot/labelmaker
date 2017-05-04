package com.hand.dot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class Config {
    private String outputWorkDir;

	public String getOutputWorkDir() {
		return outputWorkDir;
	}

	public void setOutputWorkDir(String outputWorkDir) {
		this.outputWorkDir = outputWorkDir;
	}
}
