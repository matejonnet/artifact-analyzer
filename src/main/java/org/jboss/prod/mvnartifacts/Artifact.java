package org.jboss.prod.mvnartifacts;

import java.nio.file.Path;

public class Artifact implements Comparable<Artifact> {
	
	String groupId;
	String artifactId;
	String version;
	Path pom;
	
	public Artifact(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}
	
	public Artifact(String groupId, String artifactId, String version, Path pom) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.pom = pom;
	}
	
	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version + ( pom != null ? " - " + pom : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

	public String key() {
		return groupId + ":" + artifactId;
	}

	public int compareTo(Artifact o) {
		return this.key().compareTo(o.key());
	}
	
	
}
