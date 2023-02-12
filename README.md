# Grails/Groovy Application

This is a Grails/Groovy application that requires Gradle 7.6, Java 17.0.1, and Grails 5.25 to run.

## Prerequisites

Before you can run this application, you will need to make sure you have the following software installed:

- SDK Manager
- Java SDKs
- Grails SDKs
- Gradle SDKs

## Installing SDK Manager

To install SDK Manager, follow these steps:

1. Download the latest version of SDK Manager [https://sdkman.io/install](https://sdkman.io/install) .
2. Install based on instructures
3. Make sure you your system path to include sdkman executable.

## Installing SDKs with SDK Manager

Once you have installed SDK Manager, you can use it to install different versions of Java, Grails, and Gradle. Follow these steps:

1. Open a terminal or command prompt.
2. Run `sdk install java 17.0.1-open` to install Java 17.0.1.
3. Run `sdk install grails 5.25.0` to install Grails 5.25.
4. Run `sdk install gradle 7.6` to install Gradle 7.6.

## Installation

To install this application, follow these steps:

1. Clone the repository to your local machine.
2. Navigate to the root directory of the application.
3. Run `grails clean` to build the application.
4. Run `grails run-app` to run the application.

## Usage

Once the application is running, you can access it in your web browser by going to `http://localhost:8080/`.

### Running the Application in Debug Mode

To run the application in debug mode, follow these steps:

1. Open a terminal or command prompt.
2. Navigate to the root directory of the application.
3. Run `gradle run-app --debug-jvm` to start the application in debug mode.
4. Then you can hookup your IDE to start a remote JRE at port `:5050`


## Databse connectivity
In order to connect correctly to the database from the grails application you need to make sure that `grails-app/config/application.yml`
has the correct connection information for the MySQL DB.

If you are running the local Docker MySQL then make sure that the connection information matches that of your Docker


