package com.redhat.prod.artifactaligner.resolver;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.resolution.ModelResolver;

import com.redhat.prod.artifactaligner.Artifact;
import com.redhat.prod.artifactaligner.ArtifactBuilder;
import com.redhat.prod.artifactaligner.aether.ArtifactResolver;

public class PomReader {

    private ArtifactResolver resolver;
    private DefaultModelBuilderFactory modelBuilderFactory;
    private ArtifactBuilder artifactBuilder;
    
    public PomReader(File localMavenRepo, ArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder;
        resolver = new ArtifactResolver(localMavenRepo);
        modelBuilderFactory = new DefaultModelBuilderFactory();
    }
    
    public Artifact readArtifactFromPom(File pom) throws Exception {

        ModelBuilder modelBuilder = modelBuilderFactory.newInstance();

        ModelResolver mr = new CustomModelResolver(resolver);
        
        ModelBuildingRequest request = new DefaultModelBuildingRequest()
            .setProcessPlugins( false )
            .setPomFile( pom )
            .setModelResolver( mr)
            .setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );

        ModelBuildingResult result = null;
        try {
            result = modelBuilder.build(request);
        } catch(IllegalArgumentException e) {
            System.err.println("Cannot resolve pom: " + e);
        }
        
        Model model;
        if (result != null) {
            model = result.getEffectiveModel();
        } else {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new FileReader(pom));
        }
        
        Artifact artifact = getArtifact(pom, model);
        for(Dependency dependency : model.getDependencies()) {
            artifact.addDependency(
                artifactBuilder.getArtifact(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    dependency.getVersion()));
        }
        return artifact;
    }

    private Artifact getArtifact(File pom, Model model) {
        Artifact artifact = artifactBuilder.getArtifact(model.getGroupId(), model.getArtifactId(), model.getVersion());
        artifact.addPom(pom.toPath());
        return artifact;
    }
}
