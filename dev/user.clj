(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require [clojure.java.io :as io]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.reflect :refer [reflect]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [alembic.still :refer [lein]]
            [com.stuartsierra.component :as component]
            [hello-mesos.system :as sys]
            [hello-mesos.scheduler :as sched]
            [clojure.java.shell :refer [sh]]))

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
  [& [task-type]]
  (condp = task-type
    :jar (do (lein uberjar)
             (swap! configuration assoc :task-launcher sched/jar-task-info))
    :ha (do (lein uberjar)
            (swap! configuration assoc :task-launcher sched/jar-task-info))
    :shell (swap! configuration assoc :task-launcher sched/shell-task-info)
    :docker (swap! configuration assoc :task-launcher sched/docker-task-info))

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
  (init task-type)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
