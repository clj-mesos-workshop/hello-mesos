(ns hello-mesos.component.executor-driver
  (:require [clj-mesos.executor :as mesos]
            [com.stuartsierra.component :as component]))

(defrecord ExecutorDriver [executor driver]
  component/Lifecycle
  (start [component]
    (when-not driver
      (let [driver (mesos/driver executor)]
        (mesos/start driver)
        (assoc component :driver driver))))
  (stop [_]
    (when driver
      (mesos/stop driver))))

(defn new-executor-driver
  [executor]
  (map->ExecutorDriver {:executor executor}))
