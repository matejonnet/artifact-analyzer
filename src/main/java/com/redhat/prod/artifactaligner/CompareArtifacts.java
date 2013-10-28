package com.redhat.prod.artifactaligner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class CompareArtifacts {
    
    ArtifactBuilder artifactBuilder;
    
    Map<String, Set<Artifact>> localBuildsGrouped;
    Map<String, Set<Artifact>> repoGrouped;
    Map<String, Set<Artifact>> missingGrouped;
    
    public CompareArtifacts(File sourcesRoot, File missingLog, File m2Repo) throws FileNotFoundException, IOException {
        artifactBuilder = new ArtifactBuilder();
        
        List<File> sourcePoms = getPoms(sourcesRoot);
        Set<Artifact> localBuilds = readPoms(sourcePoms);
        localBuildsGrouped = groupById(localBuilds);
        
        List<File> repoPoms = getPoms(sourcesRoot);
        Set<Artifact> repo = readPoms(repoPoms);
        repoGrouped = groupById(repo);
        
        Set<Artifact> missing = parseMissing(missingLog);
        missingGrouped = groupById(missing);
        
        System.out.println("");
        System.out.println("## LOCAL BUILDS ##");
        printGrouped(localBuildsGrouped, false);

        System.out.println("");
        System.out.println("## M2 repo " + m2Repo.getAbsolutePath() + " ##");
        printGrouped(repoGrouped, false);
        
        System.out.println("");
        System.out.println("## MISSING ##");
        printGrouped(missingGrouped, true);
        
        System.out.println("");
        System.out.println("## MISSING and LOCAL BUILD ##");
        Set<String> missingAndLocalBuild = new TreeSet<>(missingGrouped.keySet());
        missingAndLocalBuild.retainAll(localBuildsGrouped.keySet());
        for (String key : missingAndLocalBuild) {
            System.out.println("");
            System.out.println("_localbuild_");
            Set<Artifact> locals = localBuildsGrouped.get(key);
            print(locals, false);
            
            System.out.println("_missing_");
            Set<Artifact> missings = missingGrouped.get(key);
            print(missings, true);
        }

        System.out.println("");
        System.out.println("## MISSING NO LOCAL BUILD - IMPORT INTO BREW ##");
        Set<String> missingNoLocalBuild = new TreeSet<>(missingGrouped.keySet());
        missingNoLocalBuild.removeAll(localBuildsGrouped.keySet());
        for (String key : missingNoLocalBuild) {
            Set<Artifact> missings = missingGrouped.get(key);
            print(missings, true);
        }
    }

    private void printGrouped(Map<String, Set<Artifact>> missingGrouped, boolean resolveReference) {
        for (Map.Entry<String, Set<Artifact>> artifactEntrys : missingGrouped.entrySet()) {
            Set<Artifact> artifacts = artifactEntrys.getValue();
            System.out.println("");
            int size = artifacts.size();
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

    private void print(Collection<Artifact> artifacts, boolean resolveReference) {
        for (Artifact artifact : artifacts) {
            System.out.print(artifact);
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

    private Set<Artifact> parseMissing(File missingLog) throws IOException {
        List<String> missing = Files.readAllLines(missingLog.toPath(), Charset.defaultCharset());
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (String line : missing) {
            //MISSING: org.rhq:safe-invoker:4.4.0 FROM:  DIR: org/rhq/safe-invoker/4.4.0
            Pattern p = Pattern.compile("^MISSING: (.*):(.*):(.*) FROM:.*");
            
            Matcher matcher = p.matcher(line);
            while (matcher.find()) {
                Artifact artifact = artifactBuilder.getArtifact(
                                                        matcher.group(1),
                                                        matcher.group(2),
                                                        matcher.group(3));
                artifacts.add(artifact); 
            }
        }
        return artifacts;
    }

    private Set<Artifact> readPoms(List<File> poms) throws IOException, FileNotFoundException {
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (File pom : poms) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pom));
                Artifact artifact = getArtifact(pom, model);
                for(Dependency dependency : model.getDependencies()) {
                    artifact.addDependency(
                        artifactBuilder.getArtifact(
                            dependency.getGroupId(),
                            dependency.getArtifactId(),
                            dependency.getVersion()));
                }
                artifacts.add(artifact); 
            } catch (XmlPullParserException e) {
                System.err.println("Parse error [" + e.getMessage() + "] in " + pom);
            }
        }
        return artifacts;
    }

    Artifact getArtifact(File pom, Model model) {
        Artifact artifact = artifactBuilder.getArtifact(getGroupId(model), model.getArtifactId(), getVersion(model));
        artifact.addPom(pom.toPath());
        return artifact;
    }

    private String getVersion(Model model) {
        String version = model.getVersion();
        if (version != null) {
            return version;
        } else {
            Parent parent = model.getParent();
            if (parent != null) {
                return parent.getVersion();
            }
        }
        return null;
    }    

    private String getGroupId(Model model) {
        String gid = model.getGroupId();
        if (gid != null) {
            return gid;
        } else {
            Parent parent = model.getParent();
            if (parent != null) {
                return parent.getGroupId();
            }
        }
        return null;
    }    
    
    private List<File> getPoms(File parent) {
        List<File> poms = new ArrayList<File>();
        for (File file : parent.listFiles()) {
            if (file.isDirectory()) {
                poms.addAll(getPoms(file));
            } else if (file.getName().toLowerCase().equals("pom.xml")) {
                poms.add(file);
            }
        }
        return poms;
    }
}
