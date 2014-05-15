package com.redhat.prod.artifactanalyzer.test;

import com.redhat.prod.artifactanalyzer.Artifact;
import com.redhat.prod.artifactanalyzer.PomDirectoryReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by matej on 5/15/14.
 */
public class AnalyzeTestCase {

    @Test
    public void referencesTest() {
        List<Artifact> m2RepoArtifacts = readRepo();
        for (Artifact artifact : m2RepoArtifacts) {
            System.out.println(artifact.key());
            if (artifact.key().startsWith("junit:junit")) {
                Assert.assertEquals("Missing references.", 1, artifact.getReferences().size());
            }

            if (artifact.key().startsWith("commons-io:commons-io")) {
                Assert.assertEquals("Missing dependencies.", 1, artifact.getDependencies().size());
            }
        }

    }

    private List<Artifact> readRepo() {
//        URL m2RepoRootUrl = AnalyzeTestCase.class.getResource("resources/m2-repo/");
        URL m2RepoRootUrl = Thread.currentThread().getContextClassLoader().getResource("m2-repo/");
        File m2RepoRoot = new File(m2RepoRootUrl.getFile());
        PomDirectoryReader repositoryReader = new PomDirectoryReader(m2RepoRoot, m2RepoRoot);
        return repositoryReader.getAllRepoArtifacts(true, null);
    }
}
