# Feedback Helper Tool
by Bhuvan Bezawada

Originally written as part of a CS5099 project at the University of St Andrews

This fork maintained by Michael Young

## Project Overview
This tool was developed as part of the project component of the MSc Computer Science degree at the University of St Andrews.
The goal of the tool is to help markers create feedback documents more efficiently and give them insight into the content of their feedback regarding phrases they use and the sentiment behind them.
The tool is built in Java and depends on some external libraries, namely:
- `Nitrite DB`
- `Neo4j`
- `Stanford NLP`
- `tablesaw Visualisations`

## Prerequisites
### Java
Please have Java 8 or higher installed on your machine. Please follow the guidance given at this [link](https://www.java.com/en/download/help/index_installing.html) for instructions on how to obtain a copy of the Java SDK.

### Maven
Please ensure that you have Maven installed on your machine. Please follow the guidance given at the [download page](https://maven.apache.org/download.cgi) for instructions on how to obtain a copy of Maven. 
Then follow the [instructions on how to install Maven](https://maven.apache.org/install.html) on your machine.

## Compiling and Running
- Once Maven is installed and you have verified the installation is working, please navigate to the root directory of the project.
- Type `mvn package` to build the jar from the source files.
- After about 30-40 seconds you should a message saying the build was successful. 
- Then navigate to the `target/` folder.
- Within this folder look for the `JAR` named `CS5099-Project-1.0-SNAPSHOT-jar-with-dependencies.jar`
- You can copy this `JAR` to anywhere on your system and rename it if you wish. Just ensure that it has a `.jar` extension.
- Double click the `JAR` to run the tool. 
    - If the double click run does not work or the tool seems to be behaving oddly, try run the tool from the command line by running `java -jar <jar_name>.jar`.

## Running Tests
- Junit was used to write tests for the software.
- To run the tests, please ensure you have the prerequisites mentioned above.
- Then run `mvn test`.

## User Guide 
- The user guide can be found at [this link](https://drive.google.com/file/d/1UgDoxDrzht1C-oGnEB52T9OMwwsOnGq9/view).

## Copyright
Copyright (C) 2020-21 by Bhuvan Bezawada
