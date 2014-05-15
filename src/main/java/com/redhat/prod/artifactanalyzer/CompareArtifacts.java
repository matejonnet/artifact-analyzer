package com.redhat.prod.artifactanalyzer;

import com.redhat.prod.artifactanalyzer.resolver.PomReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Deprecated //TODO move missing log analyzer logic
public class CompareArtifacts {

    /** grouped lists map key is artifactId without a version */
    Map<String, Set<Artifact>> localBuildsGrouped;
    Map<String, Set<Artifact>> repoGrouped;
    Map<String, Set<Artifact>> missingGrouped;

    private PomReader pomReader;
    private ArtifactSorter artifactSorter = new ArtifactSorter();

    public CompareArtifacts(ArtifactBuilder artifactBuilder, File sourcesRoot, Set<Artifact> missingArtifacts, File m2Repo) throws FileNotFoundException, Exception {
        pomReader = new PomReader(m2Repo, artifactBuilder, false); //TODO use central ?

        PomDirectory sourceRepository = new PomDirectory(sourcesRoot);
        List<File> sourcePoms = sourceRepository.getPoms();
        Set<Artifact> localBuilds = readPoms(sourcePoms);
        localBuildsGrouped = artifactSorter.groupByGA(localBuilds);

        ArtifactWriter writer = new ArtifactWriter(System.out);

        System.out.println("");
        System.out.println("## 1. SOURCE POM ANALYZE path:" + sourcesRoot.getAbsolutePath() + " ##");
        writer.writeGrouped(localBuildsGrouped, false, false, null, 0);

        PomDirectory m2Repository = new PomDirectory(m2Repo);
        List<File> repoPoms = m2Repository.getPoms();
        Set<Artifact> repo = readPoms(repoPoms);
        repoGrouped = artifactSorter.groupByGA(repo);
        
        missingGrouped = artifactSorter.groupByGA(missingArtifacts);
        
        System.out.println("");
        System.out.println("## 2. M2 WORKING REPO ANALYZE (Artifact downloaded & installed during build) path:" + m2Repo.getAbsolutePath() + " ##");
        writer.writeGrouped(repoGrouped, true, true, null, 0);
        
        System.out.println("");
        System.out.println("## 3. MISSING (Anayze of artifacts from missing.log) ##");
        writer.writeGrouped(missingGrouped, true, true, null, 0);
        
        System.out.println("");
        System.out.println("## 4. MISSING and LOCAL SOURCE (A list of version mismatch) ##");
        Set<String> missingAndLocalBuild = new TreeSet<>(missingGrouped.keySet());
        missingAndLocalBuild.retainAll(localBuildsGrouped.keySet());
        for (String key : missingAndLocalBuild) {
            System.out.println("");
            Set<Artifact> locals = localBuildsGrouped.get(key);
            writer.write(locals, false, "local:");
            
            Set<Artifact> missings = missingGrouped.get(key);
            writer.write(missings, true, "miss :");
        }

        System.out.println("");
        System.out.println("## 5. MISSING & NO LOCAL BUILD (imported binaries) ##");
        Set<Artifact> missingNoLocalBuild = new TreeSet<>(missingArtifacts);
        missingNoLocalBuild.removeAll(localBuilds);
        writer.write(missingNoLocalBuild, true, "");
    }

    private Set<Artifact> readPoms(List<File> poms) throws Exception {
        Set<Artifact> artifacts = new TreeSet<Artifact>();
        for (File pom : poms) {
            Artifact artifact = pomReader.readArtifactFromPom(pom);
            artifacts.add(artifact);
        }
        return artifacts;
    }


}
