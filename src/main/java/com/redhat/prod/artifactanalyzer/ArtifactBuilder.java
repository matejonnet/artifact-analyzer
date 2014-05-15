package com.redhat.prod.artifactanalyzer;

import java.util.HashMap;
import java.util.Map;

public class ArtifactBuilder {

	private static ArtifactBuilder instance = new ArtifactBuilder();

    private ArtifactBuilder() {}

    static ArtifactBuilder getInstance() {
		return instance;
	}

    private final Map<String, Artifact> artifacts = new HashMap<String, Artifact>();

    public Artifact getArtifact(String groupId, String artifactId, String extension, String classifier, String version) {
        Artifact artifact = artifacts.get(Artifact.key(groupId, artifactId, extension, classifier, version));
        //System.out.println("Found artifact " + artifact + " for key " + Artifact.key(groupId, artifactId, extension, classifier, version)); //TODO log trace
        if (artifact == null) {
            artifact = new Artifact(groupId, artifactId, extension, classifier, version);
            artifacts.put(artifact.key(), artifact);
        }
        return artifact;
    }

    public boolean hasArtifact(String groupId, String artifactId, String extension, String classifier, String version) {
        return artifacts.get(Artifact.key(groupId, artifactId, extension, classifier, version)) != null;
    }
}
