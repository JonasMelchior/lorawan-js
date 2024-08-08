# Open-Source LoRaWAN Join Server

## Prerequisites

Install Java 21.

Install Maven 2.

This project utilizes FIPS-approved Java Key Stores from Bouncy Castle. We cannot merge their provided jar into a new jar,
when making a production build, so we need to download the dependency separately and add it to the class path when running the application.
```
wget https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
```

## Build from source

To create a production build, call `mvn clean package -Pproduction -Dspring.profiles.active=js`.
This will build a JAR file with all the dependencies, ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR is build, you can run the application using the commands below.
Note that, the application only starts if the Bounce Castle dependency above has been installed to the system in which the path has to be provided on the command line.
### Quick Start

The command below will create default Key Stores located at `~/.lrwan_ks/`
```
java -Dspring.profiles.active=js \
-Dloader.path=<path to bc-fips jar> \
-jar lorawan-join-server-1.0.jar
```

### Specify Key Store Paths

You can also specify in what directories the different Key Stores should reside in a .yaml file with the format as specified below.
Note, that the paths specify a directory, i.e. the directory and not the Key Store file itself. The paths can be equal of different.
```
storage:
  credentials: <path1>
  rkeys: <path2>
  skeys: <path3>
  keks: <path4>
```

Provide the .yaml file as an option (`js_config`) when running the application

```
java -Dspring.profiles.active=js \
-Dloader.path=<path to bc-fips jar> \
-Djs_config=<config filepath> \
-jar lorawan-join-server-1.0.jar
```



## UI
A UI for the Join Server can be found at _insert link_.

In order to build the UI, you must create a dependency build of this Join Server

```
mvn clean package -DskipTests
```

Install Join Server dependency to local repo

```
mvn install:install-file -Dfile=target/lorawan-join-server-1.0.jar \
-DgroupId=com.jonas -DartifactId=lorawan-join-server -Dversion=1.0 \
-Dpackaging=jar -DgeneratePom=true
```