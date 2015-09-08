(ns hello-mesos.component.path-cache-state
  (:require [hello-mesos.curator-atom-state :refer [new-state clj->data]]
            [com.stuartsierra.component :as component])
  (:import org.apache.curator.utils.ZKPaths))

(defrecord PathCacheState [curator path scheduler cache]
  component/Lifecycle
  (start [component]
    (when-not cache
      (let [state (:state scheduler)
            cache (new-state curator path state)]

        ;; Initialize Data from the State Atom
        (doseq [[key data] @state]
          (let [data-path (ZKPaths/makePath path (name key))
                data (clj->data data)]
            (try
              (-> (.setData curator)
                  (.forPath data-path data))
              (catch KeeperException.NoNodeException e
                (-> (.create curator)
                    (.creatingParentContainersIfNeeded)
                    (.forPath data-path data))))))

        (.start cache)
        (assoc component :cache cache))))
  (stop [component]
    (when cache
      (.close cache)
      (assoc component :cache nil))))

(defn new-path-cache-state
  [path]
  (map->PathCacheState {:path path}))

