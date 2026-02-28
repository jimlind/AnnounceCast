#! /bin/bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn package
java -jar target/announcecast-shaded.jar input/properties