# Feedback Helper
by Michael Young & Bhuvan Bezawada

## Overview
FeedbackHelper is a tool for efficiently creating bullet-pointed feedback on student assignments. It exploits the fact that certain phrases are likely to be repeated many times when marking one assignment for a large class of students, automatically recognising frequently used phrases and making it easy to import them for new students.

The tool was developed at the University of St Andrews, and exports feedback and grade files suitable for bulk upload to [MMS](https://mms.st-andrews.ac.uk/mms/).

## Installation
To install FeedbackHelper, go to the download page at <https://www.jdeploy.com/gh/mtorpey/FeedbackHelper>, download the installer for your operating system, and run it. You will be given the option of allowing automatic updates, which will mean you always have the latest version installed. This is the recommended method.

Alternatively, you can download the tool as an executable JAR file by going to the [releases page](https://github.com/mtorpey/FeedbackHelper/releases) here on GitHub. You'll need to make sure Java 17+ is installed on your computer. You can run the JAR file using `java -jar <filename>` on the command line, or possibly by double-clicking the JAR in your desktop environment. This method doesn't allow automatic updates, file associations or other helpful OS integrations.

## Reporting problems
If you experience any problems with FeedbackHelper, please report them using the [Issue tracker](https://github.com/mtorpey/FeedbackHelper/issues) here on GitHub. Or if you're in St Andrews, send me an email or knock on my office door!

## Compiling from source (for developers)
If you want to build this from source for some reason, for example if you want to contribute some code, you'll need to make sure [Maven](https://maven.apache.org/install.html) is installed on your machine.

To compile and run the software from source, clone this repository and do the following:
- Navigate to the root directory of the project.
- Type `mvn compile` to build the software.
- Type `mvn exec:java` to run the program.

Or to create an executable jar, do the following:
- Navigate to the root directory of the project.
- Type `mvn package` to build the jar from the source files.
- Find the executable JAR file at `target/FeedbackHelper-<version>-complete.jar`.
- You can then run the file with `java -jar target/FeedbackHelper-<version>-complete.jar`.

## History
FeedbackHelper was originally written by Bhuvan Bezawada as part of a Masters project at the School of Computer Science, University of St Andrews, under the supervision of Michael Young. The original version 1.0 written for that project is still available at <https://github.com/BhuvanBezawada/CS5099-Project>.

Since 2021, the tool has been developed by Michael Young, with additional contributions from:
- Johannes Zelger
- Oluwanifemi Fadare
- Yichen Cao

In late 2025, a major reworking was done (version 5.0) that substantially altered every part of the codebase, increasing flexibility and maintainability and adding several new features.

## Copyright
Copyright 2021–2025 by Michael Young, Bhuvan Bezawada and contributors.
