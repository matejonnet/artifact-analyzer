package com.redhat.prod.artifactanalyzer;

public class LogLine {
	private String line;
	private String origin;
	
	public LogLine(String gav, String job) {
		this.line = gav;
		this.origin = job;
	}
	
	public String getLine() {
		return line;
	}
	
	public String getOrigin() {
		return origin;
	}
}
