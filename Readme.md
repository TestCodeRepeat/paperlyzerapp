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

### IProcess
A "IProcess" is used for any long-living process that is likely best run in the cloud,
processes that take hours or days to complete.

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

### Steps to Process Data
1. Load CSV file w/ Web of Science data & build Raw Paper table
   1. Build Raw Authors table by extracting author data from Raw Paper Table ~ 5min 
      1. InitializationProcess.kt  -> InitializationProcessTest.kt
2. Run Papers against Semantic Scholar API to get 2nd data point per paper
   1. extract semantic scholar authors
3. Build Author table by refining Raw Author table by removing duplicates, initials only, unidentifiable 
   1. for each Raw Author
      1. 'duplicateCheck' indicates if record has been processed yet
      2. apply years published from 
4. Build First Names table from WoS Author table (by removing duplicates, unidentifiable)
   1. add to First Names table from Semantic Scholar Author table
5. Build Gender Name Details / Gender Table from First Names table using www.gender-api.com
   1. Gender Name Details Table contains first names along with gender data applied from the API
6. Build a Gendered Author table by applying gender data from API
7. Use Gendered Authors and Raw Papers to build Gendered Papers Table
   1. Build table
8. Apply Semantic Scholar Citations to Gendered Papers
9. Apply Stem/Ssh Discipline to Gendered Papers  ~5min
10. Apply Stem/Ssh Discipline to Gendered Authors ~2min 30sec
11. Apply SJR score & H Index to Gendered Papers ~8min
12. Run CoAuthor process to add Co Author & gender ratio from Gendered Papers table ~3min 30sec 
    1. CoAuthor process generates data for each author:
    totalPapers, averageCoAuthors, firstAuthorCount, averageGenderRatioOfPapers, genderRatioOfAllCoAuthors
13. 


Find most 100 most common first names

DisciplineProcess.kt
- It takes papers with a null `discipline` value and matches it to a value
of STEM, SSH (social sciences), or NA, along with a weighted score.
  



[https://gender-api.com/]: https://gender-api.com/
