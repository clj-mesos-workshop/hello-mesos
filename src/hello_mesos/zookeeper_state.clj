(ns hello-mesos.zookeeper-state
  (:require [curator.path-cache :as pc]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component])
  (:import [org.apache.curator.utils ZKPaths]
           [org.apache.zookeeper.KeeperException]))

(defn- bytes->clj
  [byte-data]
  (edn/read-string (apply str (map #(char (bit-and % 255)) byte-data))))

(defn- clj->bytes
  [data]
  (.getBytes (pr-str data)))

(defn path->key
  [zk-path path]
  (let [base-path (str zk-path "/")]
    (keyword (subs path (count base-path)))))

(defn- to-map
  [child-data zk-state]
  (reduce (fn [child-map datum]
            (let [key (path->key (:zk-path zk-state) (.getPath datum))
                  data (bytes->clj (.getData datum))]
              (assoc child-map key data)))
          {} child-data))

(defn- write-data
  [curator path data & {:keys [overwrite]}]
  (try
    (.. curator
        (create)
        (creatingParentsIfNeeded)
        (forPath path data))
    (catch org.apache.zookeeper.KeeperException$NodeExistsException e
      (if overwrite
        (.. curator
            (setData)
            (forPath path data))))))

(defn read-state
  [zk-state key]
  (let [{:keys [curator zk-path]} zk-state
        framework (:curator curator)
        path (ZKPaths/makePath zk-path (name key))]
    (try
      (bytes->clj (.. framework
                      (getData)
                      (forPath path)))
      (catch org.apache.zookeeper.KeeperException$NoNodeException e
        nil))))


(defn update-state!
  [zk-state path f]
  (let [{:keys [curator zk-path]} zk-state
        framework (:curator curator)
        data-path (ZKPaths/makePath zk-path (name path))
        data (f (@zk-state (path->key zk-path data-path)))]
    (write-data framework data-path (clj->bytes data) :overwrite true)))

(defn- initialize-state
  [framework path initial-state]
  (doseq [[key data] initial-state]
    (let [data-path (ZKPaths/makePath path (name key))
          data (clj->bytes data)]
      (write-data framework data-path data :overwrite false))))

(defn- new-path-cache
  [curator path]
  (pc/path-cache curator path identity))

(defrecord ZookeeperState [curator initial-state zk-path path-cache]
  clojure.lang.IDeref
  (deref [zk-state]
    (to-map (.getCurrentData path-cache) zk-state))
  component/Lifecycle
  (start [zk-state]
    (when-not path-cache
      (let [framework (:curator curator)
            path-cache (new-path-cache framework zk-path)]
        (initialize-state framework zk-path initial-state)
        (.start path-cache)
        (assoc zk-state :path-cache path-cache))))
  (stop [zk-state]
    (when path-cache
      (.close path-cache)
      (assoc zk-state :path-cache nil))))

(prefer-method print-method clojure.lang.IDeref clojure.lang.IPersistentMap)

(defn new-zookeeper-state
  [initial-state path]
  (map->ZookeeperState {:initial-state initial-state :zk-path (str path "/state")}))
