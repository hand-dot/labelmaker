package com.hand.dot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class PropertiesConfig {
    private String outputWorkDir;
    private int maxPageNum;

	public int getMaxPageNum() {
		return maxPageNum;
	}

	public void setMaxPageNum(int maxPageNum) {
		this.maxPageNum = maxPageNum;
	}

	public String getOutputWorkDir() {
		return outputWorkDir;
	}

	public void setOutputWorkDir(String outputWorkDir) {
		this.outputWorkDir = outputWorkDir;
	}
}
