package com.redhat.prod.artifactaligner;

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

	/**
	 * @return combination of groupId and artifactId 
	 */
	public String key() {
		return groupId + ":" + artifactId + ":" + version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public int compareTo(Artifact o) {
		return this.key().compareTo(o.key());
	}
	
	
}
