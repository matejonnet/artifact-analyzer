package com.redhat.prod.artifactanalyzer;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static final String[] skipIfPathContains = new String[]{ "/target/" };

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run(args);
    }

    public void run(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("p", false, "Parse log.");
        options.addOption("b", false, "Parse build GAVs.");
        options.addOption("d", false, "Print only distinct artifacts from file.");
        options.addOption("a", false, "Analyze project. Use with -s and/or -r.");
        options.addOption("l", false, "List all repo artifacts.");
        options.addOption("j", true, "Join lists of artifacts.");
        options.addOption("e", true, "Aether format list.");
        options.addOption("u", "url-template", true, "Jenkins jobs.");
        options.addOption("j", "jobs", true, "Jenkins jobs.");
        options.addOption("r", "repo", true, "Maven repo root.");
        options.addOption("s", "sources", true, "Sources root.");
//        Option analyzeRepo = OptionBuilder.withLongOpt("analyze")
//                .withDescription("Print out all artifacts found in repo. Artifacts are grouped by GA")
//                .create();
//        options.addOption(analyzeRepo);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse( options, args);

        if (cmd.hasOption("p")) {
            if (!cmd.hasOption("u") || !cmd.hasOption("j")) {
                printHelp(options);
                return;
            }

            cmd.getOptionValue("p");
            String urlTemplate = cmd.getOptionValue("u");
            String[] jobs = cmd.getOptionValue("j").split(",");

            LogContent missingLog = new MissingLogDownloader(urlTemplate, jobs);
            ArtifactParser artifactParser = new MissingLogParser(missingLog.logLines);
            Set<Artifact> missingArtifacts = artifactParser.parse(ArtifactBuilder.getInstance());

            for (Artifact artifact : missingArtifacts) {
                System.out.println(artifact);
            }

        } else if (cmd.hasOption("b")) {
            if (!cmd.hasOption("u") || !cmd.hasOption("j")) {
                printHelp(options);
                return;
            }

            String urlTemplate = cmd.getOptionValue("u");
            String[] jobs = cmd.getOptionValue("j").split(",");

            LogContent buildGavs = new BuildLogDownloader(urlTemplate, jobs);
            ArtifactParser artifactParser = new ArtifactsParserGAECV(buildGavs.logLines);
            Set<Artifact> missingArtifacts = artifactParser.parse(ArtifactBuilder.getInstance());
            for (Artifact artifact : missingArtifacts) {
                System.out.println(artifact);
            }

        } else if (cmd.hasOption("d")) {
            File missingLog = new File(cmd.getOptionValue("d"));
            if (!missingLog.exists()) {
                System.out.println("Log file must be specified. [" + missingLog + "] not found.");
                printHelp(options);
                return;
            }
            ArtifactParser artifactParser = new MissingLogParser(missingLog);
            Set<Artifact> missingArtifacts = artifactParser.parse(ArtifactBuilder.getInstance());

            for (Artifact artifact : missingArtifacts) {
                System.out.println(artifact);
            }
        } else if (cmd.hasOption("l")) {
            if (!cmd.hasOption("repo")) {
                System.out.println("Repository root must be specified.");
                printHelp(options);
                return;
            } else {
                String repoRoot = cmd.getOptionValue("repo");
                File repositoryRoot = new File(repoRoot);
                PomDirectoryReader repoReader = new PomDirectoryReader(repositoryRoot);
                List<Artifact> artifacts = repoReader.getAllRepoArtifacts(true, skipIfPathContains);
                for (Artifact artifact : artifacts) {
                    ArtifactWriter artifactWritter = new ArtifactWriter(System.out);
                    artifactWritter.writeGAV(artifact);
                    System.out.println();
                }
            }
        } else if (cmd.hasOption("j")) {
            String[] lists = cmd.getOptionValue("j").split(",");
            if (lists.length < 1) {
                System.out.println("You must specify at least one file.");
                printHelp(options);
                return;
            } else {
                Set<Artifact> artifacts = new HashSet<>();
                for (String listPath : lists) {
                    File list = new File(listPath);
                    LogContent logContent = new ArtifactListReader(list);
                    ArtifactParser listParser = new ArtifactListParser(logContent.getLogLines());
                    artifacts.addAll(listParser.parse(ArtifactBuilder.getInstance()));
                }
                for (Artifact artifact : artifacts) {
                    System.out.println(artifact);
                }
            }
        } else if (cmd.hasOption("e")) {
            String listPath = cmd.getOptionValue("e");
            File list = new File(listPath);
            if (!list.exists()) {
                System.out.println("You must specify a file.");
                printHelp(options);
                return;
            } else {
                LogContent logContent = new ArtifactListReader(list);

                ArtifactParser artifactParser = new ArtifactsParserGAECV(logContent.getLogLines());
                Set<Artifact> missingArtifacts = artifactParser.parse(ArtifactBuilder.getInstance());
                for (Artifact artifact : missingArtifacts) {
                    System.out.println(artifact);
                }
            }
        } else if (cmd.hasOption("a")) {
            String srcRootPath = cmd.getOptionValue("s");
            String m2RepoRootPath = cmd.getOptionValue("r");

            if (srcRootPath == null && m2RepoRootPath == null) {
                System.out.println("Specify at least one of parameters -s or -r.");
                printHelp(options);
                return;
            }

            analyze(srcRootPath, m2RepoRootPath);
        //TODO update missing log analyzes
//        } else if (cmd.hasOption("a")) {
//            File missingLog = new File("/home/matej/workspace/soa-p/make-mead/missing.log");
//            File sourceRoot = new File("/home/matej/workspace/soa-p/repos/");
//            File m2Repo = new File("/home/matej/workspace/soa-p/m2-repo/");
//
//            ArtifactParser artifactParser = new MissingLogParser(missingLog);
//            Set<Artifact> missingArtifacts = artifactParser.parse(ArtifactBuilder.getInstance());
//
//            new CompareArtifacts(ArtifactBuilder.getInstance(), sourceRoot, missingArtifacts, m2Repo);
        } else {
            printHelp(options);
        }

    }

    private void analyze(String srcRootPath, String m2RepoRootPath) {
        ArtifactSorter artifactSorter = new ArtifactSorter();
        ArtifactWriter writer = new ArtifactWriter(System.out);

        File m2RepoRoot = null;
        Map<String, Set<Artifact>> m2RepoArtifactsGrouped = null;
        Map<String, Set<Artifact>> sourceArtifactsGrouped = null;

        if (m2RepoRootPath != null) {
            m2RepoRoot = new File(m2RepoRootPath);

            System.out.println("");
            System.out.println("Analyzing repo: " + m2RepoRoot.getAbsolutePath() + "");

            PomDirectoryReader repositoryReader = new PomDirectoryReader(m2RepoRoot, m2RepoRoot);
            List<Artifact> m2RepoArtifacts = repositoryReader.getAllRepoArtifacts(true, skipIfPathContains);

            m2RepoArtifactsGrouped = artifactSorter.groupByGA(new HashSet<>(m2RepoArtifacts));
        }

        if (srcRootPath != null) {
            File srcRoot = new File(srcRootPath);

            System.out.println("");
            System.out.println("Analyzing sources: " + srcRoot.getAbsolutePath() + "");

            PomDirectoryReader repositoryReader = new PomDirectoryReader(srcRoot, m2RepoRoot);
            List<Artifact> sourceArtifacts = repositoryReader.getAllRepoArtifacts(true, skipIfPathContains);
            sourceArtifactsGrouped = artifactSorter.groupByGA(new HashSet<>(sourceArtifacts));
        }

        if (m2RepoArtifactsGrouped != null) {
            System.out.println("");
            System.out.println("## M2 WORKING REPO ANALYZE (Artifact downloaded & installed during build) ##");
            writer.writeGrouped(m2RepoArtifactsGrouped, true, true, srcRootPath, 2);
            System.out.println("-------------------");
            System.out.println("");
        }

        if (sourceArtifactsGrouped != null) {
            System.out.println("");
            System.out.println("## SOURCE POM ANALYZE ##");
            writer.writeGrouped(sourceArtifactsGrouped, true, true, srcRootPath, 2);
            System.out.println("-------------------");
            System.out.println("");
        }
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "...", options );
    }

}
