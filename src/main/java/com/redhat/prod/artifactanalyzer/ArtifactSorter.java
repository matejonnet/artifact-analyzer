package com.redhat.prod.artifactanalyzer;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by matej on 5/13/14.
 */
public class ArtifactSorter {
    /**
     *
     * Returns Map of artifacts with different version and the same groupID/artifactID
     */
    public Map<String, Set<Artifact>> groupByGA(Set<Artifact> artifacts) {
        Map<String, Set<Artifact>> grouped = new TreeMap<String, Set<Artifact>>();

        for (Artifact artifact : artifacts) {
            String groupKey = groupKey(artifact);
            Set<Artifact> group = grouped.get(groupKey);
            if (group == null) {
                group = new TreeSet<Artifact>();
                grouped.put(groupKey, group);
            }
            group.add(artifact);
        }
        return grouped;
    }

    private String groupKey(Artifact artifact) {
        return artifact.groupId + ":" + artifact.artifactId;
    }

}
