(ns hello-mesos.scheduler-test
  (:require [hello-mesos.scheduler :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [hello-mesos.component.scheduler :refer [new-scheduler]]
            [hello-mesos.component.curator :refer [new-curator]]
            [hello-mesos.zookeeper-state :refer [new-zookeeper-state]]
            [clj-mesos.marshalling :refer :all])
  (:import [org.apache.curator.test TestingServer]))

(defn test-system
  [initial-state zookeeper-port]
  (component/system-map
   :curator (new-curator {:hosts [] :port 2181 :backup (str "zk://localhost:" zookeeper-port)})
   :zookeeper-state (component/using
                     (new-zookeeper-state initial-state "/hello-mesos-test")
                     [:curator])
   :scheduler (component/using
               (new-scheduler shell-task-info)
               [:zookeeper-state])))

(defn test-driver
  [& [state]]
  (reify
    org.apache.mesos.SchedulerDriver
    (^org.apache.mesos.Protos$Status launchTasks [this ^org.apache.mesos.Protos$OfferID offer ^java.util.Collection tasks]
      (when state
        (swap! state update-in [:launched] inc))
      org.apache.mesos.Protos$Status/DRIVER_RUNNING)
    (^org.apache.mesos.Protos$Status declineOffer [this ^org.apache.mesos.Protos$OfferID offer]
      (when state
        (swap! state update-in [:declined] inc))
      org.apache.mesos.Protos$Status/DRIVER_RUNNING)))

(defn test-resources
  [& {:keys [cpus mem] :or {cpus 2.0 mem 1024.0}}]
  [(map->proto org.apache.mesos.Protos$Offer {:id "test-1"
                                              :framework-id "test-framework"
                                              :hostname "test"
                                              :slave-id "test-slave"
                                              :resources {:cpus cpus
                                                          :mem mem}})])

(deftest scheduler-test
  (let [zk (TestingServer.)
        system (test-system {:tasks-to-run 2
                             :running-tasks #{}
                             :staging-tasks #{}}
                            (.getPort zk))
        driver-test-state (atom {:launched 0 :declined 0})
        driver (test-driver driver-test-state)
        system (component/start system)
        s (:scheduler (:scheduler system))]
    (.resourceOffers s driver (test-resources))
    (is (= 1 (:launched @driver-test-state))
        "it should call launchTasks when enough resources are available")
    (.resourceOffers s driver (test-resources :cpus 0.1))
    (is (= 1 (:declined @driver-test-state))
        "it should decline the offer when not enough cpu is provided")))
