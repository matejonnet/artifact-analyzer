package com.redhat.prod.artifactanalyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadMissingLogs {

	List<MissingArtifact> missingLog = new ArrayList<MissingArtifact>();

	
	/**
	 * urlTemplate="http://host/hudson/job/JOB-BASE-%job%/lastBuild/consoleText" 
	 * jobs="ActiveMQ,AriesBlueprint"
	 * 
	 * @param urlTemplate
	 * @param jobs
	 * @throws Exception
	 */
	public DownloadMissingLogs(String urlTemplate, String[] jobs) throws Exception {
		
		for (String job : jobs) {
			URL myUrl = new URL(urlTemplate.replace("%job%", job));	
			BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
			String line;
			System.out.println("## parsing " + job);
			while ((line = in.readLine()) != null) {
				if (line.startsWith("MISSING:")) {
					missingLog.add(new MissingArtifact(line, job));
				}
			}
			in.close();
		}
	}
	
	public void print() {
		for (MissingArtifact missing : missingLog) {
			System.out.println(missing.getGav() + "from job: " + missing.getJob());
		}
	}
}

