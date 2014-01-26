package com.redhat.prod.artifactanalyzer;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class Main {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("p", false, "Parse log.");
		options.addOption("d", false, "Print only distinct artifacts from file.");
		options.addOption("a", false, "Analyze project.");
		options.addOption("u", "url-template", true, "Jenkins jobs.");
		options.addOption("j", "jobs", true, "Jenkins jobs.");
		
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
			
			DownloadMissingLogs missingLog = new DownloadMissingLogs(urlTempalte, jobs);
			if (cmd.hasOption("d")) {
				new DistinctArtifacts(missingLog.missingLog);
			} else {
				missingLog.print();
			}
			
		} else if (cmd.hasOption("d")) {
			File missingLog = new File(cmd.getOptionValue("d"));
			if (!missingLog.exists()) {
				System.out.println("Log file must be specified. [" + missingLog + "] not found.");
				printHelp(options);
				return;
			}
			new DistinctArtifacts(missingLog);
		
		} else if (cmd.hasOption("a")) {
	        File missingLog = new File("/home/matej/workspace/soa-p/make-mead/missing.log");
	        File sourceRoot = new File("/home/matej/workspace/soa-p/repos/");
	        File m2Repo = new File("/home/matej/workspace/soa-p/m2-repo/");
	        
	        new CompareArtifacts(sourceRoot, missingLog, m2Repo);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "...", options );
	}

}
