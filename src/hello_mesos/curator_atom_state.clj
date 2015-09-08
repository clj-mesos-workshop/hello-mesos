(ns hello-mesos.curator-atom-state
  (:require [curator.path-cache :refer :all])
  (:import [org.apache.curator.framework.recipes.cache PathChildrenCacheEvent]))

(defn data->clj
  [byte-data]
  (apply str (map #(char (bit-and % 255)) byte-data)))

(defn clj->data
  [stringable]
  (.getBytes (.toString stringable)))

(defn change-listener
  [state]
  (fn [curator event]
    (let [data (.getData event)
          path (keyword (.getPath data))
          data (data->clj (.getData data))]
      (if (= (.type event) PathChildrenCacheEvent.Type/CHILD_REMOVED)
        (swap! state dissoc path)
        (swap! state assoc path data)))))

(defn new-state
  [curator path cache-atom]
  (path-cache curator path (change-listener cache-atom)))

