package com.redhat.prod.artifactanalyzer;

import java.util.Set;

public interface ArtifactParser {
	
	public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception;

}
