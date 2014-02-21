## Artifact Analyzer ##

**Reports:**

*SOURCE POM ANALYZE* contains a list of local artifacts parsed from POMs in sources

*M2 WORKING REPO ANALYZE* a list of artifacts from ocal maven repo

*MISSING* a list of artifacts parsed from file

*MISSING and LOCAL SOURCE* a list of missing artifacts that are present also in local sources

*MISSING & NO LOCAL BUILD* a list of artifacts that are not in our sources

**Report format:**

- "*" (star) indicates that attention is required at this block
- [n] before artifact shows the number of artifacts with the same id and different version
- poms[n] number of poms with the same artifact definition folowed by list of POMs
- ref (n) references to this artifact. POMs that define this as dependency (TODO: not always resolved)

## Examples command line options
    
list all repo artifacts

    -l -r ./m2-repos/leveldbjni/
    
print distinct artifacts only

    -d /home/matej/temp/filtered.txt

parse missing logs from jenkins
    
    -p -d -url-template "http://<host>/hudson/job/<job-prefix>-%job%/lastBuild/consoleText" -jobs  ActiveMQ,AriesBlueprint,AriesJMX

DistinctArtifacts

    -DmissingLog=./mead-simulator-missing.log
