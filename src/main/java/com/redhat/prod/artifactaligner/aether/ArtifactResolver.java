package com.redhat.prod.artifactaligner.aether;

import java.io.File;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import com.redhat.prod.artifactaligner.aether.util.Booter;

/**
 * Resolves a single artifact.
 */
public class ArtifactResolver {
    
    private RepositorySystem system;
    private RepositorySystemSession session;

    public ArtifactResolver(File localRepoPath) {
        system = Booter.newRepositorySystem();
        session = Booter.newRepositorySystemSession( system, localRepoPath );
    }

    public File resolve(String groupId, String artifactId, String version ) throws ArtifactResolutionException {
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact( artifact );
//        RemoteRepository repo = Booter.newCentralRepository();
//        artifactRequest.addRepository( repo );
        ArtifactResult artifactResult = system.resolveArtifact( session, artifactRequest );
        artifact = artifactResult.getArtifact();
        return artifact.getFile();
    }

}
