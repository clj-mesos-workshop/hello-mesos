(ns hello-mesos.scheduler
  (:require [clj-mesos.scheduler :as mesos]
            [clojure.tools.logging :as log]
            [hello-mesos.zookeeper-state :refer [update-state!]])
  (:import [org.apache.curator.utils ZKPaths]))

(def min-cpu 0.1)
(def min-mem 128.0)

(defn jar-task-info
  [uuid {:keys [slave-id]}]
  [{:name "hello-mesos"
    :task-id uuid
    :slave-id slave-id
    :resources {:cpus min-cpu
                :mem min-mem}
    :executor {:executor-id "hello-mesos-executor"
               :command {:shell true
                         :value "java -jar /vagrant/target/uberjar/hello-mesos-0.1.0-SNAPSHOT-standalone.jar -m hello-mesos.system executor" }}}])

(defn shell-task-info
  [uuid {:keys [slave-id]}]
  [{:name "hello-mesos"
    :task-id uuid
    :slave-id slave-id
    :resources {:cpus min-cpu
                :mem min-mem}
    :command {:shell true
              :value "while true; do echo \"Hey Mesos\"; sleep 5; done"}}])

(defn docker-task-info
  [uuid {:keys [slave-id]}]
  [{:name "hello-mesos"
    :task-id uuid
    :slave-id slave-id
    :resources {:cpus min-cpu
                :mem min-mem}
    :container {:type :docker
                :image "busybox"
                }}])

(defn resources?
  [{:keys [cpus mem]}]
  (and cpus mem
       (>= cpus min-cpu)
       (>= mem min-mem)))

(defn scheduler
  [zk-state task-launcher]
  (mesos/scheduler
   (statusUpdate [driver status]
                 (condp = (:state status)
                   :task-lost (update-state! zk-state :tasks inc)
                   false))
   (registered [driver framework-id master]
               (update-state! zk-state :framework-id (constantly framework-id)))
   (reregistered [driver master]
                 ;; (println "[reregistered]"
                 )
   (error [driver message]
          ;; (println "[error]" message)
          (update-state! zk-state :framework-id (constantly nil)))
   (resourceOffers [driver offers]
                   (doseq [offer offers]
                     ;;                  (println @zk-state)
                     ;; (println "[resourceOffers]" offer)
                     (let [uuid (str (java.util.UUID/randomUUID))]
                       (if (and (< 0 (:tasks @zk-state))
                                (resources? (:resources offer)))
                         (let [tasks (task-launcher uuid offer)]
                           (mesos/launch-tasks driver (:id offer) tasks)
                           (update-state! zk-state :tasks dec))
                         (mesos/decline-offer driver (:id offer))))))))
