(ns hello-mesos.system
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.component.executor-driver :refer [new-executor-driver]]
            [hello-mesos.component.scheduler-driver :refer [new-scheduler-driver]]
            [hello-mesos.component.leader-driver :refer [new-leader-driver]]
            [hello-mesos.component.curator :refer [new-curator]]
            [hello-mesos.component.scheduler :refer [new-scheduler]]
            [hello-mesos.executor :refer [executor]]
            [hello-mesos.scheduler :refer [scheduler] :as sched])
  (:gen-class))

(defn executor-system
  []
  (component/system-map
   :driver (new-executor-driver (executor))))

(defn scheduler-system
  [master n-tasks task-launcher]
  (component/system-map
   :scheduler (new-scheduler n-tasks task-launcher)
   :driver (component/using
            (new-scheduler-driver master)
            [:scheduler])))

(defn ha-scheduler-system
  [master n-tasks exhibitor zk-path task-launcher]
  (component/system-map
   :curator (new-curator exhibitor)
   :scheduler (new-scheduler n-tasks task-launcher)
   :leader-driver (component/using
                   (new-leader-driver zk-path master "hello-mesos" "hello-mesos")
                   [:curator :scheduler])))

(defn -main
  [command-type & [scheduler-type master n-tasks & _]]
  (let [system (condp = [command-type scheduler-type]
                 ["scheduler" "jar"] (scheduler-system master n-tasks sched/jar-task-info)
                 ["scheduler" "shell"] (scheduler-system master n-tasks sched/shell-task-info)
                 ["scheduler" "docker"] (scheduler-system master n-tasks sched/docker-task-info)
                 ["scheduler" "ha"] (ha-scheduler-system master n-tasks sched/jar-task-info)
                 ["executor" nil] (executor-system))]
    (component/start system)
    (while true
      (Thread/sleep 1000000))))
