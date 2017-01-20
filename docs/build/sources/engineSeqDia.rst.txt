
Data Engine Sequence Diagrams
=============================


.. uml::
   
   @startuml 
   Manager -> Tasker: submitRequest
   activate Tasker
   Tasker -> SessionsDB: addRequestToSession
   Tasker -> SessionsDB: addJobsToRequest
   Tasker -> JobDispatcher: submitJob(jobA)
   Tasker -> JobDispatcher: submitDepJob(jobB,jobA)
   deactivate Tasker
   JobDispatcher -> JobBoard: addJob(jobA)

   box "Worker" #DDDDDD
      participant JobConsumer
      participant JobRunner
      participant StatusReporter

   JobBoard -> JobConsumer: broadcastNewJobs()
   activate JobConsumer
   JobBoard <- JobConsumer: pickJob(jobA)

   JobConsumer -> JobRunner: doJob(jobA)
   deactivate JobConsumer
   activate JobRunner
   JobRunner -> StatusReporter: updateState(START)
   StatusReporter -> Tasker: jobState(START)
   Tasker -> SessionsDB: jobState(START)
   JobBoard <- JobConsumer: stillAlive
   JobRunner -> StatusReporter: updateProgress(50%)
   StatusReporter -> Tasker: jobProgress(50%)
   Tasker -> SessionsDB: jobProgress(50%)
   JobBoard <- JobConsumer: stillAlive
   JobRunner -> StatusReporter: updateState(DONE)
   StatusReporter -> Tasker: jobState(DONE)
   Tasker -> SessionsDB: jobDone(jobA)
   JobConsumer <- JobRunner: jobDone(jobA)
   deactivate JobRunner

   JobBoard <- JobConsumer: jobDone(jobA)
   JobDispatcher <- JobBoard: jobDone(jobA)
   JobDispatcher -> JobBoard: addJob(jobB)


   JobBoard -> JobConsumer: broadcastNewJobs()
   activate JobConsumer
   JobBoard <- JobConsumer: pickJob(jobB)

   JobConsumer -> JobRunner: doJob(jobB)
   deactivate JobConsumer
   activate JobRunner
   JobRunner -> StatusReporter: updateState(START)
   StatusReporter -> Tasker: jobState(START)
   Tasker -> SessionsDB: jobState(START)
   JobBoard <- JobConsumer: stillAlive
   JobRunner -> StatusReporter: updateProgress(50%)
   StatusReporter -> Tasker: jobProgress(50%)
   Tasker -> SessionsDB: jobProgress(50%)
   JobBoard <- JobConsumer: stillAlive
   JobRunner -> StatusReporter: updateState(FAILED)
   StatusReporter -> Tasker: jobState(FAILED)
   Tasker -> SessionsDB: jobFailed(jobB)
   JobConsumer <- JobRunner: jobFailed(jobB)
   deactivate JobRunner

   JobBoard <- JobConsumer: jobFailed(jobB)
   JobDispatcher <- JobBoard: jobFailed(jobB)

   note over JobDispatcher: cancelJobsDepOn(jobB)

   @enduml










