(ns hello-mesos.system
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.component.executor-driver :refer [new-executor-driver]]
            [hello-mesos.component.scheduler-driver :refer [new-scheduler-driver]]
            [hello-mesos.component.leader-driver :refer [new-leader-driver]]
            [hello-mesos.component.web-ui :refer [new-web-ui]]
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
  [master initial-state exhibitor task-launcher zk-path port]
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
            [:scheduler :zookeeper-state])
   :web-ui (component/using
            (new-web-ui port)
            [:zookeeper-state])))

(defn ha-scheduler-system
  [master initial-state exhibitor task-launcher zk-path port]
  (component/system-map
   :curator (new-curator exhibitor)
   :zookeeper-state (component/using
                     (new-zookeeper-state initial-state zk-path)
                     [:curator])
   :scheduler (component/using
               (new-scheduler task-launcher)
               [:zookeeper-state])
   :leader-driver (component/using
                   (new-leader-driver zk-path master "hello-mesos" "hello-mesos")
                   [:curator :scheduler])))

(defn leader?
  "Returns true if the system is currently the leader."
  [sys]
  (let [driver (-> (:leader-driver sys)
                   :driver
                   deref)]
    (and driver
         (not (keyword? driver)))))

(defn -main
  [command-type & [scheduler-type
                   master
                   n-tasks
                   exhibitor-hosts
                   exhibitor-port
                   exhibitor-backup
                   zk-path
                   web-ui-port & _]]
  (let [state {:tasks n-tasks}
        exhibitor {:hosts (into [] (clojure.string/split #"," exhibitor-hosts))
                   :port (Integer/parseInt exhibitor-port)
                   :backup exhibitor-backup}
        system (if (= "scheduler" command-type)
                 (let [task-fn (condp = scheduler-type
                                 "jar" sched/jar-task-info
                                 "shell" sched/shell-task-info
                                 "docker" sched/docker-task-info
                                 "ha" sched/shell-task-info)
                       system-fn (if (= "ha" scheduler-type) ha-scheduler-system scheduler-system)]
                   (system-fn master
                              state
                              exhibitor
                              task-fn
                              zk-path
                              web-ui-port))
                 (executor-system))]
        (component/start system)
        (while true
          (Thread/sleep 1000000))))
