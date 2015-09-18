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
    :command {:shell true
              :value "while true; do echo \"Hey Mesos\"; sleep 5; done"}
    :container {:type :docker
                :docker {:image "busybox"}}}])

(defn resources?
  [{:keys [cpus mem]}]
  (and (>= cpus min-cpu)
       (>= mem min-mem)))

(defn scheduler
  [scheduler-state task-launcher]
  (mesos/scheduler
   (statusUpdate [driver status]
                 ;; Invoked when the status of a task has changed (e.g., a slave is lost and so the task is lost, a task finishes and an executor sends a status update saying so, etc).
                 (condp = (:state status)
                   :task-lost (swap! scheduler-state update-in [:to-launch] inc)
                   (println "[statusUpdate]" status)))
   (resourceOffers [driver offers]
                   ;; Invoked when resources have been offered to this framework.
                   (doseq [offer offers]
                     (println "[resourceOffers]" offer)
                     (let [uuid (str (java.util.UUID/randomUUID))]
                       (if (and (< 0 (:to-launch @scheduler-state))
                                (resources? (:resources offer)))
                         (let [tasks (task-launcher uuid offer)]
                           (mesos/launch-tasks driver (:id offer) tasks)
                           (swap! scheduler-state update-in [:to-launch] dec))
                         (mesos/decline-offer driver (:id offer))))))
   (disconnected [driver]
                 ;; Invoked when the scheduler becomes "disconnected" from the master (e.g., the master fails and another is taking over).
                 )
   (error [driver message]
          ;; Invoked when there is an unrecoverable error in the scheduler or driver.
          )
   (executorLost [driver executor-id slave-id status]
                 ;; Invoked when an executor has exited/terminated.
                 )
   (frameworkMessage [driver executor-id slave-id data]
                     ;; Invoked when an executor sends a message.
                     )
   (offerRescinded [driver offer-id]
                   ;; Invoked when an offer is no longer valid (e.g., the slave was lost or another framework used resources in the offer).
                   )
   (registered [driver framework-id masterInfo]
               ;; Invoked when the scheduler successfully registers with a Mesos master.
               )
   (reregistered [driver masterInfo]
                 ;; Invoked when the scheduler re-registers with a newly elected Mesos master.
                 )
   (slaveLost [driver slave-id]
              ;; Invoked when a slave has been determined unreachable (e.g., machine failure, network partition).
              )))
