package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.redhat.prod.artifactanalyzer.resolver.PomReader;

public class CompareArtifacts {

    /** grouped lists map key is artifactId without a version */
    Map<String, Set<Artifact>> localBuildsGrouped;
    Map<String, Set<Artifact>> repoGrouped;
    Map<String, Set<Artifact>> missingGrouped;

    private PomReader pomReader;
    private ArtifactBuilder artifactBuilder;

    public CompareArtifacts(File sourcesRoot, File missingLog, File m2Repo) throws FileNotFoundException, Exception {
        artifactBuilder = new ArtifactBuilder();
        pomReader = new PomReader(m2Repo, artifactBuilder);
        
        List<File> sourcePoms = findPoms(sourcesRoot);
        Set<Artifact> localBuilds = readPoms(sourcePoms);
        localBuildsGrouped = groupById(localBuilds);

        System.out.println("");
        System.out.println("## 1. SOURCE POM ANALYZE path:" + sourcesRoot.getAbsolutePath() + " ##");
        printGrouped(localBuildsGrouped, false, false);

        List<File> repoPoms = findPoms(m2Repo);
        Set<Artifact> repo = readPoms(repoPoms);
        repoGrouped = groupById(repo);
        
        ArtifactParser parser = new MissingLogParser(missingLog);
        Set<Artifact> missing = parser.parse(artifactBuilder);
        missingGrouped = groupById(missing);
        
        System.out.println("");
        System.out.println("## 2. M2 WORKING REPO ANALYZE (Artifact downloaded & installed during build) path:" + m2Repo.getAbsolutePath() + " ##");
        printGrouped(repoGrouped, true, true);
        
        System.out.println("");
        System.out.println("## 3. MISSING (Anayze of artifacts from missing.log) ##");
        printGrouped(missingGrouped, true, true);
        
        System.out.println("");
        System.out.println("## 4. MISSING and LOCAL SOURCE (A list of version mismatch) ##");
        Set<String> missingAndLocalBuild = new TreeSet<>(missingGrouped.keySet());
        missingAndLocalBuild.retainAll(localBuildsGrouped.keySet());
        for (String key : missingAndLocalBuild) {
            System.out.println("");
            Set<Artifact> locals = localBuildsGrouped.get(key);
            print(locals, false, "local:");
            
            Set<Artifact> missings = missingGrouped.get(key);
            print(missings, true, "miss :");
        }

        System.out.println("");
        System.out.println("## 5. MISSING & NO LOCAL BUILD (imported binaries) ##");
        Set<Artifact> missingNoLocalBuild = new TreeSet<>(missing);
        missingNoLocalBuild.removeAll(localBuilds);
        print(missingNoLocalBuild, true, "");
    }

    private void printGrouped(Map<String, Set<Artifact>> missingGrouped, boolean resolveReference, boolean printOnlyMoreThanOne) {
        for (Map.Entry<String, Set<Artifact>> artifactEntrys : missingGrouped.entrySet()) {
            Set<Artifact> artifacts = artifactEntrys.getValue();
            int size = artifacts.size();
            if (!printOnlyMoreThanOne || size > 1) {
                System.out.println("");
                if (size > 1) {
                    System.out.print("*");
                }
                System.out.print("[" + size + "]" + artifactEntrys.getKey());
                System.out.println("");
                for (Artifact artifact : artifacts) {
                    System.out.print("  ");
                    System.out.print(artifact);
                    if (resolveReference) {
                        System.out.print("  ref(" + artifact.references.size() + "):" + getReferencesPom(artifact));
                    }
                    System.out.println("");
                }
            }
        }
    }

    /**
     * 
     * Returns Map of artifacts with different version and the same groupID/artifactID 
     */
    private Map<String, Set<Artifact>> groupById(Set<Artifact> artifacts) {
        Map<String, Set<Artifact>> grouped = new TreeMap<String, Set<Artifact>>();
        
        for (Artifact artifact : artifacts) {
            String groupKey = groupKey(artifact);
            Set<Artifact> group = grouped.get(groupKey);
            if (group == null) {
                group = new TreeSet<Artifact>();
                grouped.put(groupKey, group);
            }
            group.add(artifact);
        }
        return grouped;
    }
    
    private String groupKey(Artifact artifact) {
        return artifact.groupId + ":" + artifact.artifactId;
    }

    private void print(Collection<Artifact> artifacts, boolean resolveReference, String prefix) {
        for (Artifact artifact : artifacts) {
            System.out.print(prefix + artifact);
            if (resolveReference) {
                System.out.print(" ref(" + artifact.references.size() + "):" + getReferencesPom(artifact));
            }
            System.out.println("");
        }
    }

    /**
     * Return POM referencing given artifact 
     */
    private Set<String> getReferencesPom(Artifact artifact) {
        Set<String> references = new HashSet<String>();
        for (Artifact ref : artifact.references) {
            references.add(ref.poms.toString());
        }
        return references;
    }

    private Set<Artifact> readPoms(List<File> poms) throws Exception {
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (File pom : poms) {
            try {
                Artifact artifact = pomReader.readArtifactFromPom(pom);
                artifacts.add(artifact); 
            } catch (XmlPullParserException e) {
                System.err.println("Parse error [" + e.getMessage() + "] in " + pom);
            } catch (ModelBuildingException e) {
                System.err.println("Error reading POM [" + e.getMessage() + "] in " + pom);
            }
        }
        return artifacts;
    }


    private List<File> findPoms(File parent) {
        List<File> poms = new ArrayList<File>();
        for (File file : parent.listFiles()) {
            if (file.isDirectory()) {
                poms.addAll(findPoms(file));
            } else if (file.getName().toLowerCase().equals("pom.xml") 
                    || file.getName().toLowerCase().endsWith(".pom")) {
                if (!file.getAbsoluteFile().toString().contains("/target/")) {
                    poms.add(file);
                }
            }
        }
        return poms;
    }
}
