### Bibliographic Analysis of Semantic Scholar & Web of Science papers *(work-in-progress)*

A Ktor/JVM application for long-running processes in the cloud, querying & processing large datasets against the APIs of academic research paper stores.

This deployed application can be controlled via websocket commands.  

Required to run: 
  - a MongoDB database (local/cloud)
  - a developer API key from Semantic Scholar
  - a developer API key from [https://gender-api.com/][https://gender-api.com/]
  - an initial batch of papers from Web of Science as a `.tsv` file.

(See `Application.kt` & `/tests` for initial implementations)

[https://gender-api.com/]: https://gender-api.com/
