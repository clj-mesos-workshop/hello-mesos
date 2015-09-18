(ns hello-mesos.executor
  (:require [clj-mesos.executor :as mesos])
  (:import [java.util Date]))

(defn executor
  []
  (mesos/executor
   (launchTask [driver task-info]
               ;; Invoked when a task has been launched on this executor (initiated via SchedulerDriver.launchTasks(java.util.Collection<OfferID>, java.util.Collection<Task-info>, Filters))
               (future (loop []
                         (println (Date.) "Hey Mesos!")
                         (Thread/sleep 2000)
                         (recur)))
               (mesos/send-status-update driver {:task-id (:task-id task-info)
                                                 :state :task-running}))
   (registered [driver executor-info framework-info slave-info]
               ;; Invoked once the executor driver has been able to successfully connect with Mesos.
               (println slave-info))
   (disconnected [driver]
                 ;; Invoked when the executor becomes "disconnected" from the slave (e.g., the slave is being restarted due to an upgrade).
                 )
   (error [driver message]
          ;; Invoked when a fatal error has occurred with the executor and/or executor driver.
          )
   (frameworkMessage [driver data]
                     ;; Invoked when a framework message has arrived for this executor.
                     )
   (killTask [driver task-id]
             ;; Invoked when a task running within this executor has been killed (via SchedulerDriver.killTask(TaskID)).
             )
   (reregistered [driver slave-info]
                 ;; Invoked when the executor re-registers with a restarted slave.
                 )
   (shutdown [driver]
             ;; Invoked when the executor should terminate all of it's currently running tasks.
             )))
