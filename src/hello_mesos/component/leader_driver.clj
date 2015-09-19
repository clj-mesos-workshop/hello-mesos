(ns hello-mesos.component.leader-driver
  (:require [curator.leader :refer (leader-selector)]
            [clj-mesos.scheduler :as mesos]
            [hello-mesos.zookeeper-state :refer [update-state!]]
            [com.stuartsierra.component :as component]))

(defn- framework-info
  [user name timeout id]
  (let [info {:user "vagrant"
              :name "hello-mesos"
              :checkpoint true
              :failover-time 360}]
    (if id
      (assoc info :id id)
      info)))

(defn leader-fn
  [driver scheduler master]
  (fn  [& _]
    (let [framework-id (:framework-id @(:zookeeper-state scheduler))
          info (framework-info "" "hello-mesos" 360 framework-id)
          d (mesos/driver (:scheduler scheduler)
                          info
                          master)]
      (swap! driver (constantly d))
      (mesos/run d))))

(defn loser-fn
  [driver]
  (fn [& _]
    (mesos/stop @driver)))

(defrecord LeaderDriver [path curator scheduler selector master user name driver]
  component/Lifecycle
  (start [component]
    (when-not selector
      (let [driver (atom nil)
            leader (leader-fn driver scheduler master)
            loser (loser-fn driver )
            selector (leader-selector (:curator curator) path leader :losingfn loser)]
        (.start selector)
        (assoc component :selector selector :driver driver))))
  (stop [component]
    (when selector
      (.close selector)
      (when (and @driver (not (keyword? @driver)))
        (mesos/stop @driver))
      (swap! driver (constantly nil))
      (assoc component :selector nil))))

(defn new-leader-driver
  [path master user name]
  (map->LeaderDriver {:path path :master master :user user :name name}))

