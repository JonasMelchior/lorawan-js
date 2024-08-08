# Stand-alone LoRaWAN Join Server

This is the implementation of a LoRaWAN Join Server created as part of my bachelor's thesis. 
The solution utilizes key stores from Bouncy Castle to secure the root key(s) and derived session keys in a cost-effective yet secure manner for large scale IoT projects.

That does not mean the overall solution is secure, and this should not be used in production since it is still experimental.

## Quickstart

Run the Join Server using Docker Compose. 

```
sudo docker-compose up -d
```

This will create and run three containers on the host: 

1. The Join Server on port 7090. Check out the documentation for the REST service on 'localhost:7090/docs'
2. The UI for the Join Server on port 8080
3. A PostgreSQL database 

#### Default login: admin@gmail/admin

### Test the Join Server out

There has already been created a device on the account. You can test out a Join Procedure by sending the request below:
```
curl --location --request POST 'http://localhost:7090/lrwan/join' \
--header 'Content-Type: application/json' \
--data-raw '{
  "ProtocolVersion": "1.0",
  "SenderID": "000000",
  "ReceiverID": "D84BCF5B9FAF1803",
  "TransactionID": 3502070497,
  "MessageType": "JoinReq",
  "MACVersion": "1.0",
  "DevAddr": "00F3BFBF",
  "PHYPayload": "000318AF9F5BCF4BD80103000000000000ADE74C3F6B8D",
  "DLSettings": "00",
  "RxDelay": 1,
  "DevEUI": "0000000000000301",
  "CFList": "184f84e85684b85e84886684586e8400"
}'
```

A response with the derived session keys and other fields should be returned. You can also check the Join Log for the device in the UI.

## Dev and Build Prerequisites

Java 18.

Maven 3.

This project utilizes FIPS-approved Java Key Stores from Bouncy Castle. We cannot merge their provided jar into a new jar,
when making a production build, so we need to download the dependency separately and add it to the class path when running the application.
```
wget https://repo1.maven.org/maven2/org/bouncycastle/bc-fips/1.0.2.4/bc-fips-1.0.2.4.jar
```

## Build from source

### Prerequisites

Create the appropriate application-<profile>.properties files and subsequent database setup.

To create a production build, call `mvn clean package -Pproduction -Dspring.profiles.active=js`.
This will build JAR files for the Join Server and UI, ready to be deployed. The files can be found in the `target` folders in the different modules after the build has completed.

Once the JAR is build, you can run the application using the commands below.
Note that, the application only starts if the Bounce Castle dependency above has been installed to the system in which the path has to be provided on the command line.
### Default Java Key Stores

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
