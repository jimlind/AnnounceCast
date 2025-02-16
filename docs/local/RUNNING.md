Set the JAVA_HOME so that java and maven tools can use it.
This feels like the best way to do this so that I'm not creating system wide problems.
I've installed the it via homebrew so this is where it lives on my computer.

The parameter is the file that contains the settings. This gives us flexibility on the production environment to put
them in a space that is more protected. That file is never committed. An example properties.tmp exists as an example of
the formatting.

> export JAVA_HOME=/opt/homebrew/opt/openjdk@21
> mvn package && java -jar target/announcecast.jar input/properties