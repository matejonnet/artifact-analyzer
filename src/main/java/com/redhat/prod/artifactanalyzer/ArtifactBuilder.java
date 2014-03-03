package com.redhat.prod.artifactanalyzer;

import java.util.HashMap;
import java.util.Map;

public class ArtifactBuilder {
	
	private static ArtifactBuilder instance = new ArtifactBuilder();
	
	static ArtifactBuilder getInstance() {
		return instance;
	}
	
    private Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
    
    public Artifact getArtifact(String groupId, String artifactId, String classifier, String version) {
        Artifact artifact = artifacts.get(Artifact.key(groupId, artifactId, classifier, version));
        if (artifact == null) {
            artifact = new Artifact(groupId, artifactId, classifier, version);
            artifacts.put(artifact.key(), artifact);
        }
        return artifact;
    }
    
    public boolean hasArtifact(String groupId, String artifactId, String classifier, String version) {
        return artifacts.get(Artifact.key(groupId, artifactId, classifier, version)) != null;
    }
}
