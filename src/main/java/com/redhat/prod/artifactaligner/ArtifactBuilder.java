package com.redhat.prod.artifactaligner;

import java.util.HashMap;
import java.util.Map;

public class ArtifactBuilder {
	Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
	
	Artifact getArtifact(String groupId, String artifactId, String version) {
		Artifact artifact = artifacts.get(Artifact.key(groupId, artifactId, version));
		if (artifact == null) {
			artifact = new Artifact(groupId, artifactId, version);
			artifacts.put(artifact.key(), artifact);
		}
		return artifact;
	}
}
