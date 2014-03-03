package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.redhat.prod.artifactanalyzer.resolver.PomReader;

public class Main {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("p", false, "Parse log.");
		options.addOption("b", false, "Parse build GAVs.");
		options.addOption("d", false, "Print only distinct artifacts from file.");
		options.addOption("a", false, "Analyze project.");
		options.addOption("l", false, "List all repo artifacts.");
		options.addOption("u", "url-template", true, "Jenkins jobs.");
		options.addOption("j", "jobs", true, "Jenkins jobs.");
		options.addOption("r", "repo", true, "Maven repo root.");
		
		CommandLineParser parser = new BasicParser(); 
		CommandLine cmd = parser.parse( options, args);
		
		if (cmd.hasOption("p")) {
			if (!cmd.hasOption("u") || !cmd.hasOption("j")) {
				printHelp(options);
				return;
			}
			
			cmd.getOptionValue("p");
			String urlTempalte = cmd.getOptionValue("u");
			String[] jobs = cmd.getOptionValue("j").split(",");
			
			LogContent missingLog = new MissingLogDownloader(urlTempalte, jobs);
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

			String urlTempalte = cmd.getOptionValue("u");
			String[] jobs = cmd.getOptionValue("j").split(",");
			
			LogContent buildGavs = new BuildLogDownloader(urlTempalte, jobs);
			ArtifactParser artifactParser = new BuildLogParser(buildGavs.logLines); 
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
		} else if (cmd.hasOption("a")) {
	        File missingLog = new File("/home/matej/workspace/soa-p/make-mead/missing.log");
	        File sourceRoot = new File("/home/matej/workspace/soa-p/repos/");
	        File m2Repo = new File("/home/matej/workspace/soa-p/m2-repo/");
	        
	        new CompareArtifacts(ArtifactBuilder.getInstance(), sourceRoot, missingLog, m2Repo);

		} else if (cmd.hasOption("l")) {
			if (!cmd.hasOption("repo")) {
				System.out.println("Repository root must be specified.");
				printHelp(options);
				return;
			} else {
				String repoRoot = cmd.getOptionValue("repo");
				File repositoryRoot = new File(repoRoot);
				RepoReader repoReader = new RepoReader(repositoryRoot);
				List<Artifact> artifacts = repoReader.getAllRepoArtifacts(true);
				for (Artifact artifact : artifacts) {
					ArtifactWriter artifactWritter = new ArtifactWriter(System.out);
					artifactWritter.writeGAV(artifact);
					System.out.println();
				}
			}
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "...", options );
	}

}
