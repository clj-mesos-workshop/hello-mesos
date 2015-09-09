(ns hello-mesos.zookeeper-state
  (:require [curator.path-cache :as pc]
            [com.stuartsierra.component :as component])
  (:import [org.apache.curator.utils ZKPaths]
           [org.apache.zookeeper.KeeperException]))

(defn- data->clj
  [byte-data]
  (apply str (map #(char (bit-and % 255)) byte-data)))

(defn- clj->data
  [stringable]
  (.getBytes (.toString stringable)))

(defn path->key
  [zk-path path]
  (let [base-path (str zk-path "/")]
    (keyword (subs path (count base-path)))))

(defn- to-map
  [child-data zk-state]
  (reduce (fn [child-map datum]
            (let [key (path->key (:zk-path zk-state) (.getPath datum))
                  data (data->clj (.getData datum))]
              (assoc child-map key data)))
          {} child-data))

(defn- write-data
  [curator path data]
  (println curator path data)
  (try
    (.. curator
        (setData)
        (forPath path data))
    (catch org.apache.zookeeper.KeeperException$NoNodeException e
      (.. curator
          (create)
          (creatingParentsIfNeeded)
          (forPath path data)))))

(defn update-state!
  [zk-state path data]
  (let [{:keys [curator zk-path]} zk-state
        framework (:curator curator)
        data-path (ZKPaths/makePath zk-path path)]
    (println zk-state framework zk-path)
    (write-data framework data-path (clj->data data))))

(defn- initialize-state
  [framework path initial-state]
  (doseq [[key data] initial-state]
    (let [data-path (ZKPaths/makePath path (name key))
          data (clj->data data)]
      (write-data framework data-path data))))

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
  (map->ZookeeperState {:initial-state initial-state :zk-path path}))

(comment
 (require '[com.stuartsierra.component :as comp])
 (require '[hello-mesos.component.curator :as c])
 (require '[hello-mesos.zookeeper-state :as zks])
 (def s (-> (c/new-curator {:port 2181 :hosts [] :backup "zk://localhost:2181"})
     comp/start
     (zks/->ZookeeperState {} "/hello-test" nil)
     comp/start
     ))
)


