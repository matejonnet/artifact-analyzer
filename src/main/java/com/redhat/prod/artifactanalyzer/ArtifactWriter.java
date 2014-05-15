package com.redhat.prod.artifactanalyzer;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArtifactWriter {

	private PrintStream out;

	public ArtifactWriter(PrintStream out) {
		this.out = out;
	}

	public void writeGAV(Artifact artifact) {
		out.print(artifact.groupId + ":" + artifact.artifactId + ":" + artifact.version);
	}

    /**
     *
     * @param missingGrouped
     * @param resolveReference
     * @param printOnlyMoreThanOne
     * @param onlyReferencedFromPath write group only if at least minNumVersionsRef of elements in group are referenced from specified path
     * @param minNumVersionsRef minimum number of versions matches onlyReferencedFromPath to print group
     */
    public void writeGrouped(Map<String, Set<Artifact>> missingGrouped, boolean resolveReference, boolean printOnlyMoreThanOne, String onlyReferencedFromPath, int minNumVersionsRef) {
        for (Map.Entry<String, Set<Artifact>> artifactEntrys : missingGrouped.entrySet()) {
            Set<Artifact> artifacts = artifactEntrys.getValue();
            if (onlyReferencedFromPath == null || minNumVersionsRef == 0 || isReferencedFromPath(artifacts, onlyReferencedFromPath, minNumVersionsRef)) {
                int size = artifacts.size();
                if (!printOnlyMoreThanOne || size > 1) {
                    System.out.println("");
                    if (size > 1) {
                        out.print("*");
                    }
                    out.print("[" + size + "]" + artifactEntrys.getKey());
                    out.println("");
                    for (Artifact artifact : artifacts) {
                        out.print("  ");
                        out.print(artifact);
                        if (resolveReference) {
                            out.print("  ref(" + artifact.getReferences().size() + "):" + getReferencesPom(artifact));
                        }
                        out.println("");
                    }
                }
            }
        }
    }

    private boolean isReferencedFromPath(Set<Artifact> artifacts, String referencedFromPath, int minNumRef) {
        int refFound = 0;
        for (Artifact artifact : artifacts) {
            Set<Artifact> references = artifact.getReferences();
            referencesLoop:
            for (Artifact reference : references) {
                Set<Path> poms = reference.getPoms();
                for (Path pom : poms) {
                    if (pom.startsWith(referencedFromPath)) {
                        refFound ++;
                        break referencesLoop;
                    }
                }
            }
        }
        return refFound >= minNumRef;
    }

    public void write(Collection<Artifact> artifacts, boolean resolveReference, String prefix) {
        for (Artifact artifact : artifacts) {
            System.out.print(prefix + artifact);
            if (resolveReference) {
                System.out.print(" ref(" + artifact.references.size() + "):" + getReferencesPom(artifact));
            }
            System.out.println("");
        }
    }

    /**
     * Return POM referencing given artifact
     */
    private Set<String> getReferencesPom(Artifact artifact) {
        Set<String> references = new HashSet<String>();
        for (Artifact ref : artifact.references) {
            references.add(ref.poms.toString());
        }
        return references;
    }

}
