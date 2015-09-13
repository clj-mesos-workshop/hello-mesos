(defproject hello-mesos "0.1.0-SNAPSHOT"
  :description "Introduction to Mesos Framework using Clojure"
  :url "https://github.com/clj-mesos-workshop/hello-mesos"
  :license {:name "MIT"
            :url "https://github.com/edpaget/hello-mesos/LICENSE"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-mesos "0.22.0"]
                 [curator "0.0.6"]
                 [com.stuartsierra/component "0.2.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [leiningen "2.5.1"]]
  :local-repo ".m2"
  ;;:main ^:skip-aot gorilla-test.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [alembic "0.3.2"]]
                   :source-paths ["dev"]
                   ;: global-vars {*warn-on-reflection* true}
                   :repl-options {:port 8999}}
             :user {:plugins [[refactor-nrepl "1.0.5"]
                              [cider/cider-nrepl "0.9.1"]
                              [refactor-nrepl "1.1.0"]
                              [lein-gorilla "0.3.4"]]}
             :uberjar {:aot :all}})
