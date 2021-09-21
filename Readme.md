### Bibliographic Analysis of Semantic Scholar & Web of Science papers *(work-in-progress)*

A Ktor/JVM application for managing long-running processes in the cloud. Query & process large 
datasets against the APIs of major academic research paper stores: **Web of Science** & **Semantic Scholar**.

This deployed application can be controlled via websocket commands.

### Required to run:

- a MongoDB database (local/cloud)
  - a developer API key from Semantic Scholar
  - a developer API key from [https://gender-api.com/][https://gender-api.com/]
  - an initial batch of papers from Web of Science as a `.tsv` file.

### Websocket Commands
`ws://localhost:8080/allthethings`

`stats` `start` `stop` `reset`

Once running either locally or in the cloud, trigger processes by connecting via Web
socket and send plain text commands from a basic websocket client.
 
See `PaperlyzerApp.kt`, `Sockets.kt` & `/tests` for initial implementations

[https://gender-api.com/]: https://gender-api.com/
