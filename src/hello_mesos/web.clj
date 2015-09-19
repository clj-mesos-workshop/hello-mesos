(ns hello-mesos.web
  (:require [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :as resp]
            [net.cgrand.enlive-html :as html]
            [compojure.route :as route]
            [hello-mesos.zookeeper-state :refer [update-state!]]))

(html/defsnippet task-li "templates/task_li.html"
  [:li]
  [task]
  [:.content] (html/content task))

(html/deftemplate main-template "templates/application.html"
  [state running staging]
  [:head :title] (html/content "Hello Mesos")
  [:body :div.container html/any-node] (html/replace-vars state)
  [:body :ul#running-tasks] (html/content (map task-li running))
  [:body :ul#staging-tasks] (html/content (map task-li staging)))

(defn index-page
  [zk-state req]
  (let [state @zk-state
        stringified-state {:tasks-to-run (str (:tasks-to-run state))}
        running-tasks (:running-tasks state)
        staging-tasks (:staging-tasks state)]
    (main-template stringified-state running-tasks staging-tasks)))

(defn update-state
  [zk-state {:keys [form-params]}]
  (let [{:strs [tasks-to-run]} form-params]
    (update-state! zk-state :tasks-to-run (constantly (Integer/parseInt tasks-to-run)))
    (resp/redirect "/" 303)))

(defn router
  [zk-state]
  (wrap-defaults (routes
                  (GET "/" request (index-page zk-state request))
                  (POST "/" request (update-state zk-state request)))
                 (update-in site-defaults [:security] assoc :anti-forgery false)))
