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
            [hello-mesos.system :as sys]
            [hello-mesos.scheduler :as sched]))

(def configuration (atom {:master "zk://10.10.4.2:2181/mesos"
                          :tasks 1
                          :task-launcher sched/jar-task-info}))

(defn- get-config [k]
  (if-not @configuration
    (println "You have not set the configuration variable yet.")
    (get @configuration k)))

(def system
  "A Var containing an object representing the application under
  development."
  nil)

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'system (constantly (sys/scheduler-system (get-config :master)
                                                             (get-config :tasks)
                                                             (get-config :task-launcher)))))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (alter-var-root #'system component/start))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (alter-var-root #'system component/stop))

(defn go
  "Initializes and starts the system running."
  [& [task-type]]
  (when-let [task-fn (if (or (keyword? task-type) (nil? task-type))
                       (condp = task-type
                         nil sched/jar-task-info
                         :jar sched/jar-task-info
                         :shell sched/shell-task-info
                         :docker  sched/docker-task-info)
                       task-type)]
    (when (or (nil? task-type) (= :jar task-type))
      (lein uberjar))
    (swap! configuration assoc :task-launcher task-fn))
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
