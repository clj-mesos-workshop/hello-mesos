(ns hello-mesos.system
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.component.executor-driver :refer [new-executor-driver]]
            [hello-mesos.component.scheduler-driver :refer [new-scheduler-driver]]
            [hello-mesos.component.scheduler :refer [new-scheduler]]
            [hello-mesos.executor :refer [executor]]
            [hello-mesos.scheduler :refer [scheduler]])
  (:gen-class))

(defn executor-system
  []
  (component/system-map
   :driver (new-executor-driver (executor))))

(defn scheduler-system
  [master n-tasks]
  (component/system-map
   :scheduler (new-scheduler n-tasks)
   :driver (component/using
            (new-scheduler-driver master)
            [:scheduler])))

(defn -main
  [command-type & [master n-tasks & _]]
  (let [system (condp = command-type
                 "scheduler" (scheduler-system master n-tasks)
                 "executor" (executor-system))]
    (component/start system)
    (while true
      (Thread/sleep 1000000))))
