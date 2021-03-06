(defproject hello-mesos "0.1.0-SNAPSHOT"
  :description "Lein template for Apache Mesos Frameworks"
  :url "https://github.com/clj-mesos-workshop/hello-mesos"
  :license {:name "MIT"
            :url "https://github.com/clj-mesos-workshop/hello-mesos/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-mesos "0.22.0"]
                 [curator "0.0.6"]
                 [http-kit "2.1.19"]
                 [compojure "1.4.0"]
                 [enlive "1.1.6"]
                 [ring/ring-defaults "0.1.5"]
                 [org.apache.curator/curator-test "2.8.0"]
                 [com.stuartsierra/component "0.2.3"]]
  :target-path "target/%s"
  :local-repo ".m2"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.7"]
                                  [alembic "0.3.2"]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all}})
