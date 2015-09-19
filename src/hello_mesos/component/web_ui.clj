(ns hello-mesos.component.web-ui
  (:require [com.stuartsierra.component :as component]
            [hello-mesos.web :refer [router]]
            [org.httpkit.server :refer [run-server]]))

(defrecord WebUI [zookeeper-state port server]
  component/Lifecycle
  (start [component]
    (if server
      component
      (let [s (run-server (router zookeeper-state) {:port port :json? false})]
        (assoc component :server s))))
  (stop [component]
    (if-not server
      component
      (do (server :timeout 100)
          (assoc component server nil)))))

(defn new-web-ui
  [port]
  (map->WebUI {:port port}))
