# Distributed Music Library Application
### SOFE 4790U: Distributed Systems Final Group Project
| Tahmid Chowdhury | Kevaun Harris | Hamzi Farhat | Fernando Chan Qui |
|------------------|------------------|------------------|------------------|
| 100822671        | 100822219     | 100831450    | 100844946         |
| tahmid.chowdhury1@ontariotechu.net    | kevaun.harris@ontariotechu.net    | hamzi.farhat@ontariotechu.net   | fernando.chanqui@ontariotechu.net     |

## Description

Welcome to the Distributed Music Library Application, a scalable and feature-rich system built using Java RMI. This innovative project transforms the traditional music library into a distributed, interactive platform that allows multiple clients to search for songs, stream them, contribute new tracks, view metadata, and rate their favorite music.

## Prerequisites

Java Development Kit (JDK)

JavaFX SDK 22.0.2

Any Integrated Development Environment (IDE) and/or terminal

## Instructions to Set Up and Run the Application
<br>


### Step 1: Download and Setup JavaFX SDK

Download JavaFX SDK version 22.0.2 from the official website.

Extract the SDK and save it directly to your C: drive, resulting in the path ``` C:/javafx-sdk-22.0.2. ```

<br>


### Step 2: Compile the Common Files

Navigate to the project directory in your terminal and compile the common Java files:

```javac common/*.java```

<br>


### Step 3: Compile and Run the Server

Compile the server files with the following command:

```javac --module-path "C:/javafx-sdk-22.0.2/lib" --add-modules javafx.controls,javafx.media server/RMIServerImpl.java server/Server.java```

Start the server using the command:

```java --module-path "C:/javafx-sdk-22.0.2/lib" --add-modules javafx.controls,javafx.media server.Server```

<br>

### Step 4: Compile and Run the Client

Compile the client files with the following command:

```javac --module-path "C:/javafx-sdk-22.0.2/lib" --add-modules javafx.controls,javafx.media client/PeerInfoImpl.java client/MusicLibraryGUI.java client/Client.java```

Start the client application using the command:

```java --module-path "C:/javafx-sdk-22.0.2/lib" --add-modules javafx.controls,javafx.media client.Client```

<br>

### Step 5: Interact with the Application

Once the server and client applications are running:

Use the client GUI to search for songs, play them, or add new tracks.

Multiple clients can connect simultaneously, leveraging the system's multithreaded architecture.

## Notes

Ensure that both the server and client are running on the same machine or within the same network.


Follow the instructions above to enjoy a distributed music library experience!

