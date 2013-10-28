package com.redhat.prod.artifactaligner;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        File missingLog = new File("/home/matej/workspace/soa-p/make-mead/missing.log");
        File sourceRoot = new File("/home/matej/workspace/soa-p/repos/");
        File m2Repo = new File("/home/matej/workspace/soa-p/m2-repo/");
        
        new CompareArtifacts(sourceRoot, missingLog, m2Repo);

    }
}
