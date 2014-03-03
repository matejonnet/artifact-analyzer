package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class ArtifactListReader extends LogContent {
	
	public ArtifactListReader(File file) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
		for (String line : lines) {
        	if (line.startsWith("#")) {
        		continue;
        	}
			logLines.add(new LogLine(line, file.getName()));
		}
	}
}
