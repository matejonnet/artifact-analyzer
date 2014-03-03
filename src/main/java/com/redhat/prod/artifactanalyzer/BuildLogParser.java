package com.redhat.prod.artifactanalyzer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildLogParser implements ArtifactParser {

	private List<LogLine> logLines;

	public BuildLogParser(List<LogLine> missingLogLines) {
		this.logLines = missingLogLines;
	}
	
	@Override
	public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception {
        
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (LogLine missingArtifact : logLines) {
        	//GAV=org.apache.cxf:cxf-wstx-msv-validation:jar:javadoc:2.7.0
            Pattern p = Pattern.compile("^GAV=(.+?):(.+?):(.*):(.+)");
            
            Matcher matcher = p.matcher(missingArtifact.getLine());
            while (matcher.find()) {
                Artifact artifact = artifactBuilder.getArtifact(
                                                        matcher.group(1),
                                                        matcher.group(2),
                                                        "",
                                                        matcher.group(4));
                artifact.addJob(missingArtifact.getOrigin());
                artifacts.add(artifact); 
            }
        }
        return artifacts;
	}

}
