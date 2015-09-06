(ns hello-mesos.executor
  (:require [clj-mesos.executor :as mesos])
  (:import [java.util Date]))

(defn executor
  []
  (mesos/executor
   (launchTask [driver task-info]
               (future (loop []
                         (println (Date.) "Hey Mesos!")
                         (Thread/sleep 2000)
                         (recur)))
               (mesos/send-status-update driver {:task-id (:task-id task-info)
                                                 :state :task-running}))
   (registered [driver executor-info framework-info slave-info]
               (println slave-info))))
