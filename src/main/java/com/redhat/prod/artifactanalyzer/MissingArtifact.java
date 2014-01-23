package com.redhat.prod.artifactanalyzer;

public class MissingArtifact {
	private String gav;
	private String job;
	
	public MissingArtifact(String gav, String job) {
		this.gav = gav;
		this.job = job;
	}
	
	public String getGav() {
		return gav;
	}
	
	public String getJob() {
		return job;
	}
}
