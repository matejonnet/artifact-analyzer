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

    private List<LogLine> logLines;

    //TODO remove, use ListReader
    public MissingLogParser(File missingLog) throws IOException {
        List<String> missingLines = Files.readAllLines(missingLog.toPath(), Charset.defaultCharset());
        for (String line : missingLines) {
            if (line.startsWith("#")) {
                continue;
            }
            logLines.add(new LogLine(line, missingLog.getName()));
        }
    }

    public MissingLogParser(List<LogLine> missingLogLines) {
        this.logLines = missingLogLines;
    }

    @Override
    public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception {

        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (LogLine missingArtifact : logLines) {
            //org.rhq:safe-invoker:4.4.0 FROM:  DIR: org/rhq/safe-invoker/4.4.0
            Pattern p = Pattern.compile("^(.*):(.*):(.*) FROM:.*");

            Matcher matcher = p.matcher(missingArtifact.getLine());
            while (matcher.find()) {
                Artifact artifact = artifactBuilder.getArtifact(
                        matcher.group(1),
                        matcher.group(2),
                        "",
                        "",
                        matcher.group(3));
                artifact.addOrigin(missingArtifact.getOrigin());
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

}
