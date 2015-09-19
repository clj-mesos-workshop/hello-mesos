(ns hello-mesos.scheduler
  (:require [clj-mesos.scheduler :as mesos]
            [hello-mesos.zookeeper-state :refer [update-state!]]))

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
    :command {:shell true
              :value "while true; do echo \"Hey Mesos\"; sleep 5; done"}}])

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

(defn no-of-tasks
  [zk-state]
  (reduce + (map count [(:staging-tasks @zk-state) (:running-tasks @zk-state)])))

(defn launch-tasks?
  [zk-state]
  (< (no-of-tasks zk-state) (:tasks-to-run @zk-state)))

(defn tasks-to-kill
  [zk-state]
  (let [to-kill (- (no-of-tasks zk-state) (:tasks-to-run @zk-state))]
    (if (> to-kill 0)
      (take to-kill (:running-tasks @zk-state))
      nil)))

(defn scheduler
  [zk-state task-launcher]
  (mesos/scheduler
   (statusUpdate [driver status]
                 ;; Invoked when the status of a task has changed (e.g., a slave is lost and so the task is lost, a task finishes and an executor sends a status update saying so, etc).
                 (let [{:keys [task-id]} status]
                   (condp = (:state status)
                     :task-failed (do (update-state! zk-state :staging-tasks #(disj % task-id))
                                      (update-state! zk-state :running-tasks #(disj % task-id)))
                     :task-lost (do (update-state! zk-state :staging-tasks #(disj % task-id))
                                    (update-state! zk-state :running-tasks #(disj % task-id)))
                     :task-staging (do (update-state! zk-state :staging-tasks #(conj % task-id))
                                       (update-state! zk-state :running-tasks #(disj % task-id)))
                     :task-running (do (update-state! zk-state :staging-tasks #(disj % task-id))
                                       (update-state! zk-state :running-tasks #(conj % task-id)))
                     false))
                 (println "[statusUpdate]" status))
   (resourceOffers [driver offers]
                   ;; Invoked when resources have been offered to this framework.
                   (doseq [offer offers]
                     (println "[resourceOffers]" offer)

                     ;; Launch Tasks if we haven't reached our desired number and we have enough
                     ;; resources. else decline the offer
                     (if (and (launch-tasks? zk-state)
                              (resources? (:resources offer)))
                       (let [uuid (str (java.util.UUID/randomUUID))
                             tasks (task-launcher uuid offer)]
                         (mesos/launch-tasks driver (:id offer) tasks))
                       (mesos/decline-offer driver (:id offer)))

                     ;; If We've launched too many tasks kill some
                     (doseq [task-id (tasks-to-kill zk-state)]
                       (mesos/kill-task driver task-id))))
   (disconnected [driver]
                 ;; Invoked when the scheduler becomes "disconnected" from the master (e.g., the master fails and another is taking over).
                 )
   (error [driver message]
          ;; Invoked when there is an unrecoverable error in the scheduler or driver.
          ;; (println "[error]" message)
          (update-state! zk-state :running-tasks (constantly #{}))
          (update-state! zk-state :staging-tasks (constantly #{}))
          (update-state! zk-state :framework-id (constantly nil)))
   (executorLost [driver executor-id slave-id status]
                 ;; Invoked when an executor has exited/terminated.
                 )
   (frameworkMessage [driver executor-id slave-id data]
                     ;; Invoked when an executor sends a message.
                     )
   (offerRescinded [driver offer-id]
                   ;; Invoked when an offer is no longer valid (e.g., the slave was lost or another framework used resources in the offer).
                   )
   (registered [driver framework-id master-info]
               ;; Invoked when the scheduler successfully registers with a Mesos master.
<<<<<<< c94bcfa17eba829950293c03cf578bb84b6fad15
               (if (= framework-id (:framework-id @zk-state))
                 (let [tasks (map (fn [task] {:task-id task :state :task-running})
                                  (concat (:running-tasks @zk-state)
                                          (:staging-tasks @zk-state)))]
                   (when (first tasks)
                     (mesos/reconcile-tasks driver tasks)))
                 (do
                   (update-state! zk-state :staging-tasks (constantly #{}))
                   (update-state! zk-state :running-tasks (constantly #{}))
                   (update-state! zk-state :framework-id (constantly framework-id)))))
   (reregistered [driver master-Info]
                 ;; Invoked when the scheduler re-registers with a newly elected Mesos master.
                 )
   (slaveLost [driver slave-id]
              ;; Invoked when a slave has been determined unreachable (e.g., machine failure, network partition).
              )))
