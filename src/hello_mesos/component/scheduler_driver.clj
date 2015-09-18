(ns hello-mesos.component.scheduler-driver
  (:require [clj-mesos.scheduler :as mesos]
            [com.stuartsierra.component :as component]
            [hello-mesos.zookeeper-state :refer [read-state]]))

(defn- build-driver
  [scheduler zk-state master]
  (let [framework-info {:user ""
                        :name "hello-mesos"
                        :checkpoint true
                        :failover-timeout 60}
        id (read-state zk-state :framework-id)
        framework-info (if id
                        (assoc framework-info :id id)
                        framework-info)]
    (mesos/driver (:scheduler scheduler)
                  framework-info
                  master)))

(defrecord SchedulerDriver [master scheduler driver zookeeper-state]
  component/Lifecycle
  (start [component]
    (when-not driver
      (let [driver (build-driver scheduler zookeeper-state master)]
        (mesos/start driver)
        (assoc component :driver driver))))
  (stop [component]
    (when driver
      (mesos/abort driver)
      (assoc component :driver nil))))

(defn new-scheduler-driver
  [master]
  (map->SchedulerDriver {:master master}))
