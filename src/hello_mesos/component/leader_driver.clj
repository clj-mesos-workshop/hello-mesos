(ns hello-mesos.component.leader-driver
  (:require [curator.leader :refer (leader-selector)]
            [clj-mesos.scheduler :as mesos]
            [com.stuartsierra.component :as component]))

(defn leader-fn
  [driver leader? scheduler master]
  (fn  [& _]
    (println (:zookeeper-state scheduler))
    (println @(:zookeeper-state scheduler))
    (println "I AM LEADER")
    (let [framework-id (:framework-id @(:zookeeper-state scheduler))
          d (mesos/driver (:scheduler scheduler)
                          {:user "" :name "hello-mesos" :framework-id framework-id}
                          master)]
      (println d)
      (println framework-id)
      (swap! driver (constantly d))
      (swap! leader? (constantly true))
      (mesos/run d))))

(defn loser-fn
  [driver leader?]
  (fn [& _]
    (swap! leader? (constantly false))
    (mesos/stop @driver)))

(defrecord LeaderDriver [path curator scheduler selector master user name driver leader?]
  component/Lifecycle
  (start [component]
    (when-not selector
      (let [leader? (atom false)
            driver (atom nil)
            leader (leader-fn driver leader? scheduler master)
            loser (loser-fn driver leader?)
            selector (leader-selector (:curator curator) path leader :losingfn loser)]
        (.start selector)
        (assoc component :selector selector :driver driver :leader? leader?))))
  (stop [component]
    (when selector
      ((loser-fn driver leader?) nil nil)
      (.close selector)
      (assoc component :selector nil :driver nil :leader? nil))))

(defn new-leader-driver
  [path master user name]
  (map->LeaderDriver {:path path :master master :user user :name name}))

