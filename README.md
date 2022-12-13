# Feedback Helper
by Bhuvan Bezawada

Originally written as part of a CS5099 project at the University of St Andrews

Later contributions by Michael Young and Johannes Zelger

This fork maintained by Michael Young

## Project Overview
This tool was developed as part of the project component of the MSc Computer Science degree at the University of St Andrews.
The goal of the tool is to help markers create feedback documents more efficiently and give them insight into the content of their feedback regarding phrases they use and the sentiment behind them.
The tool is built in Java and depends on some external libraries, namely:
- `Nitrite DB`
- `tablesaw Visualisations`

## How to run
The easiest way to get the tool running is to use the binary release from Github.
- Make sure you have Java installed (see below).
- Go to the [releases page](https://github.com/mtorpey/FeedbackHelper/releases).
- Download `FeedbackHelper-<version>-complete.jar`.
- You can copy this `JAR` to anywhere on your system and rename it if you wish. Just ensure that it has a `.jar` extension.
- Double click the `JAR` to run the tool.
    - If double-clicking does not work or the tool seems to be behaving oddly, try run the tool from the command line by running `java -jar <jar_name>.jar`.
    - You might need to set the file to be executable, which you can do from a Unix command-line with `chmod +x <jar_name>.jar`.

## Prerequisites
### Java
Feedback Helper requires Java to be installed on your machine.  Debian/Ubuntu users can install a recent Java version with `sudo apt install default-jdk`, while Windows and Mac users might download it from [Amazon Corretto](https://aws.amazon.com/corretto/).

Any Java version 8 or higher should be sufficient.  Java 17 is recommended.  You can check what version you are running with `java --version`.

### Maven
If compiling from source, please ensure that you have Maven installed on your machine. Please follow the guidance given at the [download page](https://maven.apache.org/download.cgi) for instructions on how to obtain a copy of Maven.
Then follow the [instructions on how to install Maven](https://maven.apache.org/install.html) on your machine.

## Compiling from source
To compile and run the software from source, download this repository and do the following:
- Navigate to the root directory of the project.
- Type `mvn compile` to build the software.
- Type `mvn exec:java` to run the program.

Or to create an executable jar, do the following:
- Navigate to the root directory of the project.
- Type `mvn package` to build the jar from the source files.
- After about 10 seconds you should a message saying the build was successful.
- Then navigate to the `target/` folder.
- Within this folder look for the `JAR` named `FeedbackHelper-<version>-complete.jar`

## Running Tests
- JUnit was used to write tests for the software.
- To run the tests, please ensure you have the prerequisites mentioned above.
- Then run `mvn test`.

## User Guide
- The user guide can be found at [this link](https://drive.google.com/file/d/1UgDoxDrzht1C-oGnEB52T9OMwwsOnGq9/view).

## Copyright
Copyright (C) 2020-2022 by Bhuvan Bezawada and contributors
