package com.redhat.prod.artifactaligner;

import java.util.HashMap;
import java.util.Map;

public class ArtifactBuilder {
    private Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
    
    public Artifact getArtifact(String groupId, String artifactId, String version) {
        Artifact artifact = artifacts.get(Artifact.key(groupId, artifactId, version));
        if (artifact == null) {
            artifact = new Artifact(groupId, artifactId, version);
            artifacts.put(artifact.key(), artifact);
        }
        return artifact;
    }
    
    public boolean hasArtifact(String groupId, String artifactId, String version) {
        return artifacts.get(Artifact.key(groupId, artifactId, version)) != null;
    }
}
