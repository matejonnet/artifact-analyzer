package com.redhat.prod.artifactanalyzer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse list formated groupId:artifactId:extension:classifier:version
 * org.apache.cxf:cxf-wstx-msv-validation:jar:javadoc:2.7.0
 *
 * @author Matej Lazar
 */
public class ArtifactsParserGAECV implements ArtifactParser {

    private final List<LogLine> logLines;

    public ArtifactsParserGAECV(List<LogLine> missingLogLines) {
        this.logLines = missingLogLines;
    }

    //org.apache.cxf:cxf-wstx-msv-validation:jar:javadoc:2.7.0
    private static final Pattern aetherPattern = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");
    //private static final Pattern mvnPattern = Pattern.compile("(?:wrap:)?mvn:([^/ ]+)/([^/ ]+)/([^/\\$ ]*)(/([^/\\$ ]+)(/([^/\\$ ]+))?)?(\\$.+)?");

    @Override
    public Set<Artifact> parse(ArtifactBuilder artifactBuilder) throws Exception {

        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (LogLine logLine : logLines) {
            Matcher m = aetherPattern.matcher(logLine.getLine());
            if (!m.matches()) {
                //TODO
                continue;
            }
            String groupId = m.group(1);
            String artifactId = m.group(2);
            String version = m.group(7);
            String extension = m.group(4);
            String classifier = m.group(6);

//            if (present(classifier)) {
//                if (present(extension)) {
//                    b.append("/").append(extension);
//                } else {
//                    b.append("/jar");
//                }
//                b.append("/").append(classifier);
//            } else if (present(extension)) {
//                b.append("/").append(extension);
//            }


            Artifact artifact = artifactBuilder.getArtifact(
                    groupId,
                    artifactId,
                    extension,
                    classifier,
                    version);
            artifact.addOrigin(logLine.getOrigin());
            artifacts.add(artifact);
        }
        return artifacts;
    }

    private static boolean present(String part) {
        return part != null && !part.isEmpty();
    }
}
