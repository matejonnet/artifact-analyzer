package org.jboss.prod.mvnartifacts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class CompareArtifacts {

	public static void main(String[] args) throws Exception {
		File missingLog = new File("/home/matej/workspace/soa-p/make-mead/missing.log");
		File sourceRoot = new File("/home/matej/workspace/soa-p/repos/");

		new CompareArtifacts(sourceRoot, missingLog);

	}

	
	public CompareArtifacts(File rootDir, File missingLog) throws FileNotFoundException, IOException {
		List<File> poms = getPoms(rootDir);
		Map<String, Artifact> localBuilds = readLocalBuilds(poms);
		Set<Artifact> missing = parseMissing(missingLog);
		
		Set<Artifact> missingAndLocalBuild = new HashSet<Artifact>(missing);
		missingAndLocalBuild.retainAll(localBuilds.values());
		
		System.out.println("=== LOCAL BUILDS ===");
		print(localBuilds.values());

		System.out.println("=== MISSING ===");
		print(missing);
		
		System.out.println("=== LOCAL BUILD & MISSING ===");
		
		for (Artifact artifact : missingAndLocalBuild) {
			Artifact local = localBuilds.get(artifact.key());
			System.out.println(artifact + " <> " + local);
		}
	}


	private void print(Collection<?> list) {
		for (Object line : list) {
			System.out.println(line);
		}
	}


	private Set<Artifact> parseMissing(File missingLog) throws IOException {
		List<String> missing = Files.readAllLines(missingLog.toPath(), Charset.defaultCharset());
		Set<Artifact> artifacts = new HashSet<Artifact>();
		for (String line : missing) {
			//MISSING: org.rhq:safe-invoker:4.4.0 FROM:  DIR: org/rhq/safe-invoker/4.4.0
			Pattern p = Pattern.compile("^MISSING: (.*):(.*):(.*) FROM:.*");
			
			Matcher matcher = p.matcher(line);
			while (matcher.find()) {
				Artifact artifact = new Artifact(
						matcher.group(1),
						matcher.group(2),
						matcher.group(3));
				artifacts.add(artifact); 
			}
		}
		return artifacts;
	}


	private Map<String, Artifact> readLocalBuilds(List<File> poms) throws IOException, FileNotFoundException {
		Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
		for (File pom : poms) {
			try {
				MavenXpp3Reader reader = new MavenXpp3Reader();
				Model model = reader.read(new FileReader(pom));
				//System.out.println(getGroupId(model) + ":" + model.getArtifactId() + ":" + getVersion(model) + " - " + pom);
				Artifact artifact = new Artifact(getGroupId(model), model.getArtifactId(), getVersion(model), pom.toPath());
				artifacts.put(artifact.key(), artifact); 
			} catch (XmlPullParserException e) {
				System.err.println("Parse error [" + e.getMessage() + "] in " + pom);
			}
		}
		return artifacts;
	}


	private String getVersion(Model model) {
		String version = model.getVersion();
		if (version != null) {
			return version;
		} else {
			Parent parent = model.getParent();
			if (parent != null) {
				return parent.getVersion();
			}
		}
		return null;
	}	

	private String getGroupId(Model model) {
		String gid = model.getGroupId();
		if (gid != null) {
			return gid;
		} else {
			Parent parent = model.getParent();
			if (parent != null) {
				return parent.getGroupId();
			}
		}
		return null;
	}	
	
	private List<File> getPoms(File parent) {
		List<File> poms = new ArrayList<File>();
		for (File file : parent.listFiles()) {
			if (file.isDirectory()) {
				poms.addAll(getPoms(file));
			} else if (file.getName().toLowerCase().equals("pom.xml")) {
				poms.add(file);
			}
		}
		return poms;
	}
}
