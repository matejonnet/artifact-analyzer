package com.redhat.prod.artifactanalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-06-03.
 */
public class Project implements Comparable {

    private String name;
    private List<Artifact> artifacts;
    private List<Project> dependencies = new ArrayList<>();

    public Project(String name, List<Artifact> artifacts) {
        this.name = name;
        this.artifacts = artifacts;
    }

    public void resolveDependencies(List<Project> projects) {
        projectsLoop:
        for (Project project : projects) {
            for (Artifact artifact : project.artifacts) {
                if (this.depends(artifact)) {
                    addProjectDependency(project);
                    continue projectsLoop;
                }
            }
        }
    }

    /**
     *
     * @param artifact
     * @return True if one of local artifacts depends on given artifact
     */
    private boolean depends(Artifact artifact) {
        for (Artifact localArtifact : artifacts) {
            if (localArtifact.dependenciesContainGA(artifact)) {
                return true;
            }
        }
        return false;
    }

    private void addProjectDependency(Project project) {
        dependencies.add(project);
    }

    public List<Project> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

}
