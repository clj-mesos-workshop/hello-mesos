(defproject hello-mesos "0.1.0-SNAPSHOT"
  :description "Introduction to Mesos Framework using Clojure"
  :url "https://github.com/clj-mesos-workshop/hello-mesos"
  :license {:name "MIT"
            :url "https://github.com/edpaget/hello-mesos/LICENSE"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [bdisraeli/clj-mesos "0.20.9-SNAPSHOT"]
                 [com.stuartsierra/component "0.2.2"]
                 [leiningen "2.5.1"]]
  :main ^:skip-aot gorilla-test.core
  ;; :jvm-opts ["-Djava.library.path=/path/to/libmesos.{so,dylib}"]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]]
                   :source-paths ["dev"]}
             :user {:plugins [[refactor-nrepl "1.0.5"]
                              [cider/cider-nrepl "0.9.1"]
                              [refactor-nrepl "1.1.0"]
                              [lein-gorilla "0.3.4"]]}
             :uberjar {:aot :all}})
