(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [leiningen.core.project :as p]
   [leiningen.uberjar :refer [uberjar]]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [hello-mesos.system :as sys]))

(defn load-project
  []
  (p/read (str (io/file (System/getProperty "user.dir") "./project.clj"))))

(defn compile-uberjar
  []
  (let [project (load-project)]
    (try (uberjar project 'hello-mesos.system)
         (catch Exception e
           (.printStackTrace e)))))

(def system
  "A Var containing an object representing the application under
  development."
  nil)

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (compile-uberjar)
  (alter-var-root #'system (constantly (sys/scheduler-system "localhost:5050" 1))))

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
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after 'user/go))
