package com.redhat.prod.artifactanalyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class BuildLogDownloader extends LogContent {

	/**
	 * urlTemplate="http://host/hudson/job/JOB-BASE-%job%/ws/archive/build-info.txt" 
	 * jobs="ActiveMQ,AriesBlueprint"
	 * 
	 * @param urlTemplate
	 * @param jobs
	 * @throws Exception
	 */
	public BuildLogDownloader(String urlTemplate, String[] jobs) throws Exception {
		
		for (String job : jobs) {
			URL myUrl = new URL(urlTemplate.replace("%job%", job));	
			System.out.println("Reading " + job + " from: " + myUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(myUrl.openStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("GAV=")) {
					logLines.add(new LogLine(line, job));
				}
			}
			in.close();
		}
	}
	
}

