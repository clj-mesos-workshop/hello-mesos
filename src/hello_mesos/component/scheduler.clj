(ns hello-mesos.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.scheduler :as sched]))

(defrecord Scheduler [task-launcher zookeeper-state scheduler]
  component/Lifecycle
  (start [component]
    (when-not scheduler
      (let [scheduler (sched/scheduler zookeeper-state task-launcher)]
        (assoc component :scheduler scheduler))))
  (stop [component]
    (when scheduler
      (assoc component :scheduler nil))))

(defn new-scheduler
  [task-launcher]
  (map->Scheduler {:task-launcher task-launcher}))

