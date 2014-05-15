package com.redhat.prod.artifactanalyzer.resolver;

import com.redhat.prod.artifactanalyzer.Artifact;
import com.redhat.prod.artifactanalyzer.ArtifactBuilder;
import com.redhat.prod.artifactanalyzer.aether.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.resolution.ModelResolver;

import java.io.File;
import java.io.FileReader;

public class PomReader {

    private final DefaultModelBuilderFactory modelBuilderFactory;
    private File repositoryRoot;
    private final ArtifactBuilder artifactBuilder;
    private boolean useCentral;

    /**
     *
     * @param repositoryRoot
     * @param artifactBuilder
     * @param useCentral use maven central for resolving parent
     */
    public PomReader(File repositoryRoot, ArtifactBuilder artifactBuilder, boolean useCentral) {
        this.repositoryRoot = repositoryRoot;
        this.artifactBuilder = artifactBuilder;
        this.useCentral = useCentral;
        modelBuilderFactory = new DefaultModelBuilderFactory();
    }

    public Artifact readArtifactFromPom(File pom) throws Exception {

        Model model = null;
        if (repositoryRoot != null) {
            model = resolveModelUsingRepo(pom);
        }
        if (model == null) {
            //TODO log
            System.out.println("Warn: using fallback to simple model resolver for: " + pom);
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new FileReader(pom));
        }

        Artifact artifact = getArtifact(pom, model);
        for(Dependency dependency : model.getDependencies()) {
            artifact.addDependency(
                artifactBuilder.getArtifact(
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
                    //dependency.getType(),
                    "", //ignore type
                    //dependency.getClassifier(),
                    "", //ignore classifier
                    dependency.getVersion()));
        }
        return artifact;
    }

    private Model resolveModelUsingRepo(File pom) {
        Model model = null;
        ArtifactResolver resolver = new ArtifactResolver(repositoryRoot, useCentral);
        ModelBuilder modelBuilder = modelBuilderFactory.newInstance();

        ModelResolver mr = new CustomModelResolver(resolver);

        ModelBuildingRequest request = new DefaultModelBuildingRequest()
                .setProcessPlugins(false)
                .setPomFile(pom)
                .setModelResolver(mr)
                .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                .setSystemProperties(System.getProperties())
                .setTwoPhaseBuilding(true);

        ModelBuildingResult result = null;
        try {
            result = modelBuilder.build(request);
        } catch (Exception e) {
            System.err.println("Cannot read pom: " + pom + " " + e.getMessage());
            e.printStackTrace();
        }

        if (result != null) {
            model = result.getEffectiveModel();
        }
        return model;
    }

    private Artifact getArtifact(File pom, Model model) {
        Artifact artifact = artifactBuilder.getArtifact(model.getGroupId(), model.getArtifactId(), "", "", model.getVersion());
        artifact.addPom(pom.toPath());
        return artifact;
    }
}
