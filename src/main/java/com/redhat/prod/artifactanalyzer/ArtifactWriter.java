package com.redhat.prod.artifactanalyzer;

import java.io.PrintStream;

public class ArtifactWriter {

	private PrintStream out;

	public ArtifactWriter(PrintStream out) {
		this.out = out;
	}

	public void writeGAV(Artifact artifact) {
		out.print(artifact.groupId + ":" + artifact.artifactId + ":" + artifact.version);
	}

}
