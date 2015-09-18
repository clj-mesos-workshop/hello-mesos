(ns hello-mesos.component.curator
    (:require [curator.framework :refer (curator-framework)]
              [curator.exhibitor :refer (exhibitor-ensemble-provider exhibitors)]
              [com.stuartsierra.component :as component]))

(defrecord Curator [exhibitor-config exhibitor-connection curator]
  component/Lifecycle
  (start [component]
    (when-not curator
      (let [{:keys [hosts port backup]} exhibitor-config
            exhibitor-connection (exhibitors hosts port backup)
            provider (exhibitor-ensemble-provider exhibitor-connection)
            _ (.pollForInitialEnsemble provider)
            curator (curator-framework "" :exhibitor-provider provider)]
        (.start curator)
        (assoc component :exhibitor-connection exhibitor-connection :curator curator))))
  (stop [component]
    (when curator
      (.close curator)
      (assoc component :curator nil :exhibitor-connection nil))))

(defn new-curator
  [exhibitor]
  (map->Curator {:exhibitor-config exhibitor}))

