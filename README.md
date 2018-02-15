# SQUIRE: SPARQL Query Recommendations by Example

This is a library that, given a SPARQL query that can be satisfied by a certain RDF dataset, recommends semantically similar queries that can be satisfied by another dataset.

## Building
You can build the project using Maven 3 and a Java 8 SDK. 

First checkout this project.

    git clone https://github.com/carloallocca/Squire.git

Enter the directory you just checked out.

    cd Squire

From the project directory you can build all the modules by running

    mvn install -Dmaven.test.skip=true

When all unit tests work offline, we will stop recommending the `-Dmaven.test.skip` flag.

If you want to build only some modules individually:

* __`squire`__ : the core implementation, build this first.
* __`launcher`__ : command-line Java application as executable JAR file, depends on squire
* __`websquire`__ : Web Service version with HTTP API available as a WAR archive.

## Running
After building the project:
* __Command Line__ : execute the JAR file in `launcher/target` to get the command syntax.
* __Web Service__ : drop the WAR file found in `websquire/target` into your Web container.

### Command line
Enter the directory containing the JAR

    cd launcher/target
    
Run the executable JAR once to get the list of available commands

    java -jar org.mksmart.squire.squire-launcher-{version}.jar
    
(`version` can be something like `1.1-SNAPSHOT`)

For example, to tell it to index two SPARQL endpoints:

    java -jar org.mksmart.squire.squire-launcher-{version}.jar index \
    http://opendatacommunities.org/sparql \
    http://data.admin.ch/query/
    
After indexing them, use them to recommend the equivalent of a SPARQL query that works on the first endpoint (`-s`) to the target endpoint (`-t`):

    java -jar org.mksmart.squire.squire-launcher-{version}.jar \
    -s http://opendatacommunities.org/sparql \
    -t http://data.admin.ch/query/ \
    recommend "SELECT DISTINCT ?s ?p ?o WHERE {?s a <http://data.ordnancesurvey.co.uk/ontology/admingeo/District> . ?s ?p ?o }"
    
This process will generate a report after it ends. If you also want it to print a log to file while it executes, add the `-l` option.

## Authors
* __Carlo Allocca__, Samsung Inc.
* __Alessandro Adamou__, NUI Galway
* __Mathieu d'Aquin__, NUI Galway

Credit goes to the Knowledge Media Institute of The Open University, UK, for kickstarting this project.

## Permissions
You can use, fork and republish this software as you like so long as you retain these permissions and give us credit, preferably by citing this open access paper:

    Carlo Allocca, Alessandro Adamou, Mathieu d'Aquin, Enrico Motta:
    SPARQL Query Recommendations by Example. ESWC (Satellite Events) 2016: 128-133, 
    https://doi.org/10.1007/978-3-319-47602-5_26