Flux Concurrent Scheduling Sample
===

This sample demonstrates a concurrent approach to scheduling Flux workflows that requires high throughput and coordinated executions.
The dataset used in this sample comes from Amazon S3 bucket "1000genomes":http://aws.amazon.com/1000genomes/. It uses Amazon S3 APIs to retrieve objects and schedule them using a java action in Flux.

This sample implements Flux best practices when dealing with java actions. Java action is a double-edged sword, while offering more
power for advanced users, it is sometimes tricky to implement how to gracefully return them to Flux when involving calls to external
services which Flux has no control over. This can be implemented using flowContext.isInterrupted() "API":http://support.flux.ly/javadoc/flux/FlowContext.html#isInterrupted().
Interrupting the workflow would gracefully shutdown resources and exit from execution normally. This may be useful in testing too,
users can interrupt it from Flux Operations console to skip current execution and proceed to next steps.

Setup
===

* Download and Install Flux from [here](https://flux.ly/download)
* Install flux.jar to your local maven repo:

```
    mvn install:install-file -DgroupId=flux -DartifactId=flux -Dversion=8.0.11 -Dpackaging=jar -Dfile=flux.jar
```
* Engine configuration is defined in engine-config.properties and runtime-config.properties, make changes appropriately.

Testing
===

ConcurrentTest is a standalone test case which runs using an in-memory H2 database by default. Configuration for Postgres and MySQL are provided.

```
  mvn test -Djava.awt.headless=true
```

For MySQL

```
  mvn test -Ddatabase=mysql -DclearEngine=true -Djava.awt.headless=true
```

For Postgres

```
mvn test -Ddatabase=postgres -Djava.awt.headless=true
```

There are two workflows used: parent and child. Child workflow template is stored in Flux repository. Parent workflow spins off child instance for each S3 bucket.
