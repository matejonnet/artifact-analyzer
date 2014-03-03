package com.redhat.prod.artifactanalyzer;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Artifact implements Comparable<Artifact> {
    
    String groupId;
    String artifactId;
    String version;
    Set<Path> poms = new HashSet<Path>();
    /*list of builds from which this artifact is from */
    Set<String> jobs = new HashSet<>();
    
    Artifact parent;
    
    Set<Artifact> dependencies = new TreeSet<Artifact>();
    
    /** back reference to dependencies */
    Set<Artifact> references = new HashSet<Artifact>();
	
    private String classifier;
    
    public Artifact(String groupId, String artifactId, String classifier, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
		this.classifier = classifier;
        this.version = version;
    }
    
    @Override
    public String toString() {
        String star = poms.size() > 1 ? "*" : "";
        String pomString = poms.size() > 0 ? " " + star + "poms[" + poms.size() + "] " + poms : "";
        String jobString = jobs.size() > 0 ? " jobs: ["  + jobs + "] " : "";
		return groupId + ":" + artifactId + ":" + version + pomString + jobString;
    }

    /**
     * @return combination of groupId and artifactId 
     */
    public String key() {
        return key(groupId, artifactId, classifier, version);
    }

    public static String key(String groupId, String artifactId, String classifier, String version) {
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

    public void addDependency(Artifact dependency) {
        dependency.references.add(this);
        dependencies.add(dependency);
    }
    
    public boolean hasDependency(Artifact artifact) {
        return dependencies.contains(artifact);
    }

    public void addPom(Path path) {
        poms.add(path);
    }

	public void addJob(String job) {
		jobs.add(job);
	}
}
