(ns hello-mesos.component.leader-driver
  (:require [curator.leader :refer (leader-selector)]
            [clj-mesos.scheduler :as mesos]
            [com.stuartsierra.component :as component]))

(defn leader-fn
  [driver]
  (fn [_ _]
    (mesos/run driver)))

(defn loser-fn
  [driver]
  (fn [_ _]
    (mesos/stop driver)))

(defrecord LeaderDriver [path zookeeper scheduler selector master user name driver]
  component/Lifecycle
  (start [component]
    (when-not selector
      (let [driver (mesos/driver (:scheduler scheduler)
                                 {:user user :name name}
                                 master)
            leader (leader-fn driver)
            loser (loser-fn driver)
            selector (leader-selector (:curator zookeeper) path leader :losingfn loser)]
        (.start selector)
        (assoc component :selector selector :driver driver))))
  (stop [component]
    (when selector
      ((loser-fn driver) nil nil)
      (.close selector)
      (assoc component :selector nil :driver nil))))

(defn new-leader-driver
  [path master user name]
  (map->LeaderDriver {:path path :master master :user user :name name}))

