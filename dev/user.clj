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
            [clojure.tools.namespace.repl :as repl]
            [com.stuartsierra.component :as component]
            [hello-mesos.system :as sys]
            [hello-mesos.zookeeper-state :refer [update-state!]]
            [hello-mesos.scheduler :as sched]
            [curator.leader :refer [interrupt-leadership]]
            [clojure.java.shell :refer [sh]]))

(defn refresh
  [& opts]
  (remove-method print-method clojure.lang.IDeref)
  (apply repl/refresh opts))

(def configuration (atom {:master "zk://10.10.4.2:2181/mesos"
                          :exhibitor {:hosts []
                                      :port 2181
                                      :backup "zk://localhost:2181"}
                          :state {:tasks 1}
                          :task-launcher sched/jar-task-info
                          :zk-path "/hm"}))

(defn- get-config [k]
  (if-not @configuration
    (println "You have not set the configuration variable yet.")
    (get @configuration k)))

(defn- get-full-config
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

(defn leader?
  [sys]
  (let [driver (-> (:leader-driver sys)
                   :driver
                   deref)]
    (and driver
         (not (keyword? driver)))))

(defn followers
  "The inactive schedulers in HA mode"
  []
  (filter (comp not leader?) systems))

(defn leader
  "The leading scheduler in HA mode"
  []
  (first (filter leader? systems)))

(defn cycle-leader
  []
  (let [{:keys [leader-driver]} (leader)]
    (swap! (:driver leader-driver) #(clj-mesos.scheduler/abort %))
    (interrupt-leadership (:selector leader-driver))))

(defn stop-all
  [_]
  (let [non-leading (mapv component/stop (followers))
        leading (component/stop (leader))]
    (conj non-leading leading)))

(defn init-ha
  "Creates and initializes the systems under development in the Var
  #'systems."
  []
  (alter-var-root #'systems (->> (repeatedly #(apply sys/ha-scheduler-system (get-full-config)))
                                 (take 3)
                                 (into [])
                                 constantly)))

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'systems (constantly nil))
  (alter-var-root #'system (constantly (apply sys/scheduler-system (get-full-config)))))

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

(defn fetch-task-type
  [task-type]
  (if (or (keyword? task-type) (nil? task-type))
    (condp = task-type
      nil (do (lein uberjar) sched/jar-task-info)
      :jar (do (lein uberjar) sched/jar-task-info)
      :shell sched/shell-task-info
      :docker  sched/docker-task-info)
    task-type))

(defn go-ha
  [& [task-type]]
  (when task-type
    (swap! configuration assoc :task-launcher (fetch-task-type task-type)))
  (init-ha)
  (start)
  :ready-ha)

(defn go
  "Initializes and starts the system running."
  [& [task-type]]
  (when task-type
    (swap! configuration assoc :task-launcher (fetch-task-type task-type)))
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (let [after-fn (if systems 'user/go-ha 'user/go)]
    (refresh :after after-fn)))
