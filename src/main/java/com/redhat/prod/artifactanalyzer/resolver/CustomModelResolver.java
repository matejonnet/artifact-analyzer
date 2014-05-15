package com.redhat.prod.artifactanalyzer.resolver;

import com.redhat.prod.artifactanalyzer.aether.ArtifactResolver;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.File;

public class CustomModelResolver implements ModelResolver {

    ArtifactResolver resolver;
    
    public CustomModelResolver(ArtifactResolver resolver) {
        this.resolver = resolver; 
    }
    
    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) {
        File pomFile = null;
        try {
            pomFile = resolver.resolve(groupId, artifactId, version);
        } catch (ArtifactResolutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return new FileModelSource(pomFile);
    }

    @Override
    public void addRepository(Repository repository)
            throws InvalidRepositoryException {
        // TODO Auto-generated method stub
    }

    @Override
    public ModelResolver newCopy() {
        // TODO Auto-generated method stub
        return null;
    }

}
