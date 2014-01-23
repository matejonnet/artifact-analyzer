package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MissingLogParser implements ArtifactParser {

	private List<MissingArtifact> missing;

	public MissingLogParser(File missingLog) throws IOException {
		List<String> missingLines = Files.readAllLines(missingLog.toPath(), Charset.defaultCharset());
		for (String line : missingLines) {
        	if (line.startsWith("#")) {
        		continue;
        	}
			missing.add(new MissingArtifact(line, missingLog.getName()));
		}
	}
	
	public MissingLogParser(List<MissingArtifact> missing) {
		this.missing = missing;
	}
	
	@Override
	public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception {
        
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (MissingArtifact missingArtifact : missing) {
            //MISSING: org.rhq:safe-invoker:4.4.0 FROM:  DIR: org/rhq/safe-invoker/4.4.0
            Pattern p = Pattern.compile("^MISSING: (.*):(.*):(.*) FROM:.*");
            
            Matcher matcher = p.matcher(missingArtifact.getGav());
            while (matcher.find()) {
                Artifact artifact = artifactBuilder.getArtifact(
                                                        matcher.group(1),
                                                        matcher.group(2),
                                                        matcher.group(3));
                artifact.addJob(missingArtifact.getJob());
                artifacts.add(artifact); 
            }
        }
        return artifacts;
	}

}
