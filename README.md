# SQUIRE: SPARQL Query Recommendations by Example

This is a library that, given a SPARQL query that can be satisfied by a certain RDF dataset, recommends semantically similar queries that can be satisfied by another dataset.

## Building
You can build the project using Maven 3 and a Java 8 SDK. These are the modules that can be built.

* __`squire`__ : the core implementation, build this first.
* __`launcher`__ : command-line Java application as executable JAR file, depends on squire
* __`websquire`__ : Web Service version with HTTP API available as a WAr archive.

## Running
After building the project:
* __Command Line__ : execute the JAR file in `launcher/target` to get the command syntax.
* __Web Service__ : drop the WAR file found in `websquire/target` into your Web container.

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