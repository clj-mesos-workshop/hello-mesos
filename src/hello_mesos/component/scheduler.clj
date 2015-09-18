(ns hello-mesos.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.scheduler :as sched]))

(defrecord Scheduler [number-of-tasks task-launcher state scheduler]
  component/Lifecycle
  (start [component]
    (when-not scheduler
      (let [state (atom {:to-launch number-of-tasks})
            scheduler (sched/scheduler state task-launcher)]
        (assoc component :state state :scheduler scheduler))))
  (stop [component]
    (when scheduler
      (assoc component :state nil :scheduler nil))))

(defn new-scheduler
  [number-of-tasks task-launcher]
  (map->Scheduler {:number-of-tasks number-of-tasks :task-launcher task-launcher}))
