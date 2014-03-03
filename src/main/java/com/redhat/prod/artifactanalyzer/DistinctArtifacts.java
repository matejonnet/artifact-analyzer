package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.util.List;
import java.util.Set;

@Deprecated
public class DistinctArtifacts {

	ArtifactBuilder artifactBuilder = new ArtifactBuilder();

	public DistinctArtifacts(File missingLog) throws Exception {
		ArtifactParser parser = new MissingLogParser(missingLog);
        parseMissing(parser);
    }

	public DistinctArtifacts(List<LogLine> missingLog) throws Exception {
		ArtifactParser parser = new MissingLogParser(missingLog);
		parseMissing(parser);
	}

	private void parseMissing(ArtifactParser parser) throws Exception {
		Set<Artifact> missing = parser.parse(artifactBuilder);
		for (Artifact artifact : missing) {
			System.out.println(artifact);
		}
	}
	
}
