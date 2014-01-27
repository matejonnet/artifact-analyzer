package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.redhat.prod.artifactanalyzer.resolver.PomReader;

public class RepoReader {

	private File repositoryRoot;

	public RepoReader(File repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}

	public List<Artifact> getAllRepoArtifacts(boolean useCentral) {
		MavenRepository repository = new MavenRepository(repositoryRoot);
		List<File> poms = repository.getPoms();
		ArtifactBuilder artifactBuilder = new ArtifactBuilder();
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
