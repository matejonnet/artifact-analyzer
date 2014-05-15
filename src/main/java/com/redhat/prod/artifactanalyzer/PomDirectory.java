package com.redhat.prod.artifactanalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PomDirectory {

	private File root;
    private String[] skipIfPathContains;

    public PomDirectory(File root) {
		this.root = root;
    }

    public PomDirectory(File root, String[] skipIfPathContains) {
		this.root = root;
        this.skipIfPathContains = skipIfPathContains;
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
                if (!skip(file)) {
                    poms.add(file);
                }
            }
        }
        return poms;
    }

    private boolean skip(File file) {
        if (skipIfPathContains == null) {
            return false;
        }
        for (String search : skipIfPathContains) {
            if (file.getAbsoluteFile().toString().contains(search)) {
                return true;
            }
        }
        return false;
    }

}
