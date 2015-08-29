(ns hello-mesos.scheduler
  (:require [clj-mesos.scheduler :as mesos]))

(def min-cpu 0.5)
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
    :executor {:executor-id "hello-mesos-executor"
               :command {:shell true
                         :value "while true; do echo \"Hey Mesos\"; fi"}}}])

(defn docker-task-info
  [uuid {:keys [slave-id]}]
  [{:name "hello-mesos"
    :task-id uuid
    :slave-id slave-id
    :resources {:cpus min-cpu
                :mem min-mem}
    :executor {:executor-id "hello-mesos-executor"
               :command {:shell true
                         :container {:type :docker
                                     :image ""
                                     }}}}])

(defn resources?
  [{:keys [cpus mem]}]
  (and (>= cpus min-cpu)
       (>= mem min-mem)))

(defn scheduler
  [scheduler-state task-launcher]
  (mesos/scheduler
   (statusUpdate [driver status]
                 (condp = (:task-state status)
                   :task-running (println status)
                   (println status)))
   (resourceOffers [driver offers]
                   (doseq [offer offers]
                     (let [uuid (str (java.util.UUID/randomUUID))]
                       (if (and (< 0 (:to-launch @scheduler-state))
                                (resources? (:resources offer)))
                         (let [tasks (task-launcher uuid offer)]
                           (mesos/launch-tasks driver (:id offer) tasks)
                           (swap! scheduler-state update-in [:to-launch] dec))
                         (mesos/decline-offer driver (:id offer))))))))
