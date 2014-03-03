package com.redhat.prod.artifactanalyzer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.StringUtils;

public class ArtifactListParser implements ArtifactParser {

	private List<LogLine> logLines;
	private ArtifactBuilder artifactBuilder;

	public ArtifactListParser(List<LogLine> artifactsList) {
		this.logLines = artifactsList;
	}
	
	@Override
	public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception {
        
        this.artifactBuilder = artifactBuilder;
		Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (LogLine line : logLines) {
        	//org.apache.cxf:cxf-wstx-msv-validation:jar:javadoc:2.7.0
            
        	Artifact artifact; 
        	int collons = countOccurances(line.getLine(), ":");
        	if (collons == 2) {
        		artifact = parseWithoutClassifier(line.getLine());
        	} else if (collons == 3) {
        		artifact = parseWithClassifier(line.getLine());
        	} else {
        		throw new RuntimeException("Invalid line format: " + line.getLine());
        	}
		    artifact.addOrigin(line.getOrigin());
        	artifacts.add(artifact);
        }
        return artifacts;
	}

	private Artifact parseWithClassifier(String line) {
		Pattern p = Pattern.compile("^(.+?):(.+?):(.*):(.+)");
		Matcher matcher = p.matcher(line);
		if (matcher.find()) {
		    Artifact artifact = artifactBuilder.getArtifact(
		                                            matcher.group(1),
		                                            matcher.group(2),
		                                            "",
		                                            matcher.group(4));
		    return artifact;
		} else {
			throw new RuntimeException("Invalid line format: " + line);
		}
	}

	private Artifact parseWithoutClassifier(String line) {
		Pattern p = Pattern.compile("^(.+?):(.+?):(.+)");
		Matcher matcher = p.matcher(line);
		if (matcher.find()) {
		    Artifact artifact = artifactBuilder.getArtifact(
		                                            matcher.group(1),
		                                            matcher.group(2),
		                                            "",
		                                            matcher.group(3));
		    return artifact;
		} else {
			throw new RuntimeException("Invalid line format: " + line);
		}
	}

	private int countOccurances(String str, String findStr ) {
		int lastIndex=0;
		int count=0;
	
		while(lastIndex != -1) {
			lastIndex = str.indexOf(findStr,lastIndex);
			if( lastIndex != -1){
				count ++;
				lastIndex+=findStr.length();
			}
		}
		return count;
	}
}
