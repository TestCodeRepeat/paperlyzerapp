### Bibliographic Analysis of Semantic Scholar & Web of Science papers *(work-in-progress)*

A Ktor/JVM application for managing long-running processes in the cloud. Query & process large 
datasets against the APIs of major academic research paper stores: **Web of Science** & **Semantic Scholar**.

This deployed application can be controlled via websocket commands.

### Required to run:

- a MongoDB database (local/cloud)
- a developer API key from Seman tic Scholar
- a developer API key from [https://gender-api.com/][https://gender-api.com/]
- an initial batch of papers from Web of Science as a `.tsv` file.

### Websocket Commands
`ws://localhost:8080/allthethings`

`stats` `start` `stop` `reset`

Once running either locally or in the cloud, trigger processes by connecting via Web
socket and send plain text commands from a basic websocket client.
 
See `PaperlyzerApp.kt`, `Sockets.kt` & `/tests` for initial implementations

### Environmental Variables
`API_BATCH_SIZE` - # of records processed per API call
`PROCESSED_RECORDS_GOAL` - # of remaining unprocessed records which causes the process to stop.
e.g. given a batch of 50 papers, and a `UNPROCESSED_RECORDS_GOAL` of 40, the process will stop running after 10 papers
when the number of `UNPROCESSED_RECORDS_GOAL` is below 40.  If set to 0, it will run process until all target records
have been marked as 'processed'.

### Processes
Each process follow this convention:
```
interface IProcess {
    fun init()
    fun name():String
    fun runProcess()
    fun shouldContinueProcess(): Boolean
    fun printStats(outgoing: SendChannel<Frame>? = null): String
    fun cancelJobs()
    fun reset()
}
```
Where once a process is started, it continually checks for a condition to if it should continue or terminate.
These primary functions can be triggered via Websocket connection & text commands.







DisciplineProcess.kt
- It takes papers with a null `discipline` value and matches it to a value
of STEM, SSH (social sciences), or NA, along with a weighted score.
  



[https://gender-api.com/]: https://gender-api.com/
