package com.redhat.prod.artifactanalyzer;

import com.redhat.prod.artifactanalyzer.resolver.PomReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PomDirectoryReader {

    /**
     * Root folder to search for POMs.
     */
    private File rootFolder;

    /**
     * Root of structured maven repository from where artifacts can be resolved.
     */
    private File repositoryRoot = null;

    public PomDirectoryReader(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	public PomDirectoryReader(File rootFolder, File repositoryRoot) {
		this.rootFolder = rootFolder;
        this.repositoryRoot = repositoryRoot;
    }

    /**
     *
     *
     * @param useCentral
     * @param skipIfPathContains
     * @return
     */
	public List<Artifact> getAllRepoArtifacts(boolean useCentral, String[] skipIfPathContains) {
		PomDirectory repository = new PomDirectory(rootFolder, skipIfPathContains);
		/** List of all artifacts that are defined in poms under this.rootFolder */
        List<File> poms = repository.getPoms();
		ArtifactBuilder artifactBuilder = ArtifactBuilder.getInstance();
		PomReader pomReader = new PomReader(repositoryRoot, artifactBuilder, useCentral);
		List<Artifact> artifacts = new ArrayList<>();
		for (File pomFile : poms) {
			try {
				Artifact artifact = pomReader.readArtifactFromPom(pomFile);
				artifacts.add(artifact);
			} catch (Exception e) {
				System.out.println("Cannot read " + pomFile + " " + e.getMessage());
			}
		}
		return artifacts;
	}

}
