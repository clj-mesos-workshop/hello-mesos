(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require [alembic.still :refer [lein]]
            [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.java.shell :refer [sh]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [com.stuartsierra.component :as component]
            [curator.leader :refer [interrupt-leadership]]
            [hello-mesos.zookeeper-state :refer [update-state!]]
            [hello-mesos.system :as sys]
            [hello-mesos.scheduler :as sched]))

(def configuration (atom {:master "zk://10.10.4.2:2181/mesos"
                          :exhibitor {:hosts []
                                      :port 2181
                                      :backup "zk://localhost:2181"}
                          :state {:tasks 1}
                          :task-launcher sched/shell-task-info
                          :zk-path "/hello-mesos"}))

(defn- get-config [k]
  (if-not @configuration
    (println "You have not set the configuration variable yet.")
    (get @configuration k)))

(defn- config-as-vector
  []
  [(get-config :master)
   (get-config :state)
   (get-config :exhibitor)
   (get-config :task-launcher)
   (get-config :zk-path)])

(def systems
  "A Var container a vector of system to test HA modes"
  nil)

(def system
  "A Var containing an object representing the application under
  development."
  nil)

(defn followers
  "The inactive schedulers in HA mode"
  []
  (filter (comp not sys/leader?) systems))

(defn leader
  "The leading scheduler in HA mode"
  []
  (first (filter sys/leader? systems)))

(defn cycle-leader
  []
  (let [{:keys [leader-driver]} (leader)]
    (swap! (:driver leader-driver) #(clj-mesos.scheduler/abort %))
    (interrupt-leadership (:selector leader-driver))))

(defn- stop-all
  [_]
  (let [non-leading (mapv component/stop (followers))
        leading (component/stop (leader))]
    (conj non-leading leading)))

(defn zookeeper-state
  []
  (if-let [zks (:zookeeper-state (leader))]
    zks
    (:zookeeper-state system)))

(defn init-ha
  "Creates and initializes the systems under development in the Var
  #'systems."
  []
  (alter-var-root #'systems (->> (repeatedly #(apply sys/ha-scheduler-system (config-as-vector)))
                                 (take 3)
                                 (into [])
                                 constantly)))

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'systems (constantly nil))
  (alter-var-root #'system (constantly (apply sys/scheduler-system (config-as-vector)))))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (if systems
    (alter-var-root #'systems #(mapv component/start %))
    (alter-var-root #'system component/start)))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (if systems
    (alter-var-root #'systems stop-all)
    (alter-var-root #'system component/stop)))

(defn fetch-task-fn
  [task-type]
  (if (or (keyword? task-type) (nil? task-type))
    (condp = task-type
      nil sched/shell-task-info
      :jar sched/jar-task-info
      :shell sched/shell-task-info
      :docker  sched/docker-task-info)
    task-type))

(defn compile-executor
  [task-type]
  (when (= :jar task-type)
    (lein uberjar)))

(defn go
  "Initializes and starts the system running."
  [& [task-type]]
  (when-let [task-fn (fetch-task-fn task-type)]
    (compile-executor task-type)
    (swap! configuration assoc :task-launcher task-fn))
  (init)
  (start)
  :ready)

(defn go-ha
  "Initializes and starts 3 systems running in HA mode"
  [& [task-type]]
  (when-let [task-fn (fetch-task-fn task-type)]
    (compile-executor task-type)
    (swap! configuration assoc :task-launcher task-fn))
  (init-ha)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (let [after-fn (if systems 'user/go-ha 'user/go)]
    (refresh :after after-fn)))
