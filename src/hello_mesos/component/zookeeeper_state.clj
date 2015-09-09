(ns hello-mesos.component.zookeeper-state
  (:require [hello-mesos.curator-atom-state :refer [new-state clj->data]]
            [com.stuartsierra.component :as component])

(defn new-zookeeper-state
  [path state]
  (map->ZookeeperState {:path path :state state}))

