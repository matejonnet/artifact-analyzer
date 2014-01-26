package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenRepository {

	private File root;

	public MavenRepository(File root) {
		this.root = root;
	}

	public List<File> getPoms() {
		return findPoms(root);
	}
	
	private List<File> findPoms(File parent) {
        List<File> poms = new ArrayList<File>();
        for (File file : parent.listFiles()) {
            if (file.isDirectory()) {
                poms.addAll(findPoms(file));
            } else if (file.getName().toLowerCase().equals("pom.xml") 
                    || file.getName().toLowerCase().endsWith(".pom")) {
                if (!file.getAbsoluteFile().toString().contains("/target/")) {
                    poms.add(file);
                }
            }
        }
        return poms;
    }

}
