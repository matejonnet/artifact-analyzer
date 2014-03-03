package com.redhat.prod.artifactanalyzer;

import java.util.ArrayList;
import java.util.List;

class LogContent {

	List<LogLine> logLines = new ArrayList<LogLine>();

	public void printLines() {
		for (LogLine missing : logLines) {
			System.out.println(missing.getLine() + "from job: " + missing.getOrigin());
		}
	}

	public List<LogLine> getLogLines() {
		return logLines;
	}
}
