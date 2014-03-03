package com.redhat.prod.artifactanalyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MissingLogDownloader extends LogContent {

	/**
	 * urlTemplate="http://host/hudson/job/JOB-BASE-%job%/lastBuild/consoleText" 
	 * jobs="ActiveMQ,AriesBlueprint"
	 * 
	 * @param urlTemplate
	 * @param jobs
	 * @throws Exception
	 */
	public MissingLogDownloader(String urlTemplate, String[] jobs) throws Exception {
		
		for (String job : jobs) {
			URL myUrl = new URL(urlTemplate.replace("%job%", job));	
			System.out.println("Reading " + job + " from: " + myUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("MISSING:")) {
					logLines.add(new LogLine(line.replaceFirst("MISSING: ", ""), job));
				}
			}
			in.close();
		}
	}
	
}
