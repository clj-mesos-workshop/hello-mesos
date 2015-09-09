(ns hello-mesos.system
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.component.executor-driver :refer [new-executor-driver]]
            [hello-mesos.component.scheduler-driver :refer [new-scheduler-driver]]
            [hello-mesos.component.leader-driver :refer [new-leader-driver]]
            [hello-mesos.component.curator :refer [new-curator]]
            [hello-mesos.zookeeper-state :refer [new-zookeeper-state]]
            [hello-mesos.component.scheduler :refer [new-scheduler]]
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

(defn ha-scheduler-system
  [master state exhibitor task-launcher zk-path]
  (component/system-map
   :curator (new-curator exhibitor)
   :scheduler (new-scheduler state task-launcher)
   :zookeeper-state (component/using
                      (new-zookeeper-state zk-path state)
                      [:curator :scheduler])
   :leader-driver (component/using
                   (new-leader-driver zk-path master "hello-mesos" "hello-mesos")
                   [:curator :scheduler])))

(defn -main
  [command-type & [scheduler-type master n-tasks & _]]
  (let [state {:tasks n-tasks}
        system (condp = [command-type scheduler-type]
                 ["scheduler" "jar"] (scheduler-system master state sched/jar-task-info)
                 ["scheduler" "shell"] (scheduler-system master state sched/shell-task-info)
                 ["scheduler" "docker"] (scheduler-system master state sched/docker-task-info)
                 ["scheduler" "ha"] (ha-scheduler-system master state sched/jar-task-info)
                 ["executor" nil] (executor-system))]
    (component/start system)
    (while true
      (Thread/sleep 1000000))))
