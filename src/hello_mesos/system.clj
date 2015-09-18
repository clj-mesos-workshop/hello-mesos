(ns hello-mesos.system
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.component.executor-driver :refer [new-executor-driver]]
            [hello-mesos.component.scheduler-driver :refer [new-scheduler-driver]]
            [hello-mesos.component.scheduler :refer [new-scheduler]]
            [hello-mesos.component.curator :refer [new-curator]]
            [hello-mesos.zookeeper-state :refer [new-zookeeper-state]]
            [hello-mesos.executor :refer [executor]]
            [hello-mesos.scheduler :refer [scheduler] :as sched])
  (:gen-class))

(defn executor-system
  []
  (component/system-map
   :driver (new-executor-driver (executor))))

(defn scheduler-system
  [master initial-state exhibitor task-launcher zk-path]
  (component/system-map
   :curator (new-curator exhibitor)
   :zookeeper-state (component/using
                     (new-zookeeper-state initial-state zk-path)
                     [:curator])
   :scheduler (component/using
               (new-scheduler task-launcher)
               [:zookeeper-state])
   :driver (component/using
            (new-scheduler-driver master)
            [:scheduler])))

(defn -main
  [command-type & [scheduler-type master n-tasks exhibitor-hosts exhibitor-port exhibitor-backup zk-path & _]]
  (let [state {:tasks n-tasks}
        exhibitor {:hosts (into [] (clojure.string/split #"," exhibitor-hosts))
                   :port (Integer/parseInt exhibitor-port)
                   :backup exhibitor-backup}
        system (condp = [command-type scheduler-type]
                 ["scheduler" "jar"] (scheduler-system master state exhibitor sched/jar-task-info zk-path)
                 ["scheduler" "shell"] (scheduler-system master state exhibitor sched/shell-task-info zk-path)
                 ["scheduler" "docker"] (scheduler-system master state exhibitor sched/docker-task-info zk-path)
                 ["executor" nil] (executor-system))]
    (component/start system)
    (while true
      (Thread/sleep 1000000))))
