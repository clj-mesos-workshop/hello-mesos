;; gorilla-repl.fileformat = 1

;; **
;;; # Part 1: Getting Started with Clj-Mesos
;;; 
;;; Hey there! Welcome to the StrangeLoop 2015 Workshop on "Building Distributed Frameworks using Clojure and Mesos." We hope you learn a lot in the workshop and have fun! 
;;; 
;;; ## On using the Gorilla REPL worksheets
;;; 
;;; We have wanted to make the tutorial as interactive as possible live at the workshop and also allow you to refer back to the tutorial at your own pace. There are few things here to learn on how to use the worksheet format.
;;; 
;;; ### Setup
;;; 
;;; - Having the worksheet and the terminal side by side can be really useful to have the code and to see the effect quickly. You might have to restart the REPL on occassion too.
;;; 
;;; ![Setup Image](project-files/ws/images/workshop_setup.png)
;;; 
;;; - Open the [Mesos protocol](https://github.com/apache/mesos/blob/master/include/mesos/mesos.proto) file in a new tab for quick reference.
;;; 
;; **

;; **
;;; ## Loading the namespaces
;;; 
;;; We want to first load the namespaces from the `hello-mesos` project to start the Framework scheduler
;; **

;; @@
(ns stl-workshop.part-1
  (:require [user :refer :all]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Setup Configuration
;;; 
;;; We want to supply a configuration for the framework here. 
;;; 
;;; 
;;; * `mesos-master`: Where to find the Mesos URL
;;; 
;;; * `webui-port`: Where we can browse to see the status of the framework
;; **

;; @@
(reset! configuration {:mesos-master "mesos://127.0.0.1:5050"
                        :webui-port 5556
                        })
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:webui-port</span>","value":":webui-port"},{"type":"html","content":"<span class='clj-long'>5556</span>","value":"5556"}],"value":"[:webui-port 5556]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:mesos-master</span>","value":":mesos-master"},{"type":"html","content":"<span class='clj-string'>&quot;mesos://127.0.0.1:5050&quot;</span>","value":"\"mesos://127.0.0.1:5050\""}],"value":"[:mesos-master \"mesos://127.0.0.1:5050\"]"}],"value":"{:webui-port 5556, :mesos-master \"mesos://127.0.0.1:5050\"}"}
;; <=

;; @@
(init)
;; @@
;; ->
;;; Created /Users/gaupr001/work/mesos/hello-mesos/./target/uberjar+uberjar/hello-mesos-0.1.0-SNAPSHOT.jar
;;; Created /Users/gaupr001/work/mesos/hello-mesos/./target/uberjar/hello-mesos-0.1.0-SNAPSHOT-standalone.jar
;;; 
;; <-
;; =>
;;; {"type":"list-like","open":"<span class='clj-record'>#com.stuartsierra.component.SystemMap{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"list-like","open":"<span class='clj-record'>#hello_mesos.component.scheduler.Scheduler{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:number-of-tasks</span>","value":":number-of-tasks"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[:number-of-tasks 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:state</span>","value":":state"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:state nil]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:scheduler nil]"}],"value":"#hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state nil, :scheduler nil}"}],"value":"[:scheduler #hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state nil, :scheduler nil}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:driver</span>","value":":driver"},{"type":"list-like","open":"<span class='clj-record'>#hello_mesos.component.scheduler_driver.SchedulerDriver{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:master</span>","value":":master"},{"type":"html","content":"<span class='clj-string'>&quot;mesos://127.0.0.1:5050&quot;</span>","value":"\"mesos://127.0.0.1:5050\""}],"value":"[:master \"mesos://127.0.0.1:5050\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:scheduler nil]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:driver</span>","value":":driver"},{"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}],"value":"[:driver nil]"}],"value":"#hello_mesos.component.scheduler_driver.SchedulerDriver{:master \"mesos://127.0.0.1:5050\", :scheduler nil, :driver nil}"}],"value":"[:driver #hello_mesos.component.scheduler_driver.SchedulerDriver{:master \"mesos://127.0.0.1:5050\", :scheduler nil, :driver nil}]"}],"value":"#<SystemMap>"}
;; <=

;; @@
(start)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-record'>#com.stuartsierra.component.SystemMap{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"list-like","open":"<span class='clj-record'>#hello_mesos.component.scheduler.Scheduler{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:number-of-tasks</span>","value":":number-of-tasks"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[:number-of-tasks 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:state</span>","value":":state"},{"type":"html","content":"<span class='clj-atom'>#&lt;Atom@7480021b: {:to-launch 1}&gt;</span>","value":"#<Atom@7480021b: {:to-launch 1}>"}],"value":"[:state #<Atom@7480021b: {:to-launch 1}>]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"html","content":"<span class='clj-unkown'>#&lt;Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8&gt;</span>","value":"#<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>"}],"value":"[:scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>]"}],"value":"#hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}"}],"value":"[:scheduler #hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:driver</span>","value":":driver"},{"type":"list-like","open":"<span class='clj-record'>#hello_mesos.component.scheduler_driver.SchedulerDriver{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:master</span>","value":":master"},{"type":"html","content":"<span class='clj-string'>&quot;mesos://127.0.0.1:5050&quot;</span>","value":"\"mesos://127.0.0.1:5050\""}],"value":"[:master \"mesos://127.0.0.1:5050\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"list-like","open":"<span class='clj-record'>#hello_mesos.component.scheduler.Scheduler{</span>","close":"<span class='clj-record'>}</span>","separator":" ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:number-of-tasks</span>","value":":number-of-tasks"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"}],"value":"[:number-of-tasks 1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:state</span>","value":":state"},{"type":"html","content":"<span class='clj-atom'>#&lt;Atom@7480021b: {:to-launch 1}&gt;</span>","value":"#<Atom@7480021b: {:to-launch 1}>"}],"value":"[:state #<Atom@7480021b: {:to-launch 1}>]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:scheduler</span>","value":":scheduler"},{"type":"html","content":"<span class='clj-unkown'>#&lt;Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8&gt;</span>","value":"#<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>"}],"value":"[:scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>]"}],"value":"#hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}"}],"value":"[:scheduler #hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:driver</span>","value":":driver"},{"type":"html","content":"<span class='clj-unkown'>#&lt;MesosSchedulerDriver org.apache.mesos.MesosSchedulerDriver@593afc7d&gt;</span>","value":"#<MesosSchedulerDriver org.apache.mesos.MesosSchedulerDriver@593afc7d>"}],"value":"[:driver #<MesosSchedulerDriver org.apache.mesos.MesosSchedulerDriver@593afc7d>]"}],"value":"#hello_mesos.component.scheduler_driver.SchedulerDriver{:master \"mesos://127.0.0.1:5050\", :scheduler #hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}, :driver #<MesosSchedulerDriver org.apache.mesos.MesosSchedulerDriver@593afc7d>}"}],"value":"[:driver #hello_mesos.component.scheduler_driver.SchedulerDriver{:master \"mesos://127.0.0.1:5050\", :scheduler #hello_mesos.component.scheduler.Scheduler{:number-of-tasks 1, :state #<Atom@7480021b: {:to-launch 1}>, :scheduler #<Object$Scheduler$7a16741e hello_mesos.scheduler.proxy$java.lang.Object$Scheduler$7a16741e@56c7e1a8>}, :driver #<MesosSchedulerDriver org.apache.mesos.MesosSchedulerDriver@593afc7d>}]"}],"value":"#<SystemMap>"}
;; <=

;; @@
(clojure.repl/source
  hello-mesos.scheduler/scheduler)
;; @@
;; ->
;;; (defn scheduler
;;;   [scheduler-state]
;;;   (mesos/scheduler
;;;    (statusUpdate [driver status]
;;;                  (condp = (:task-state status)
;;;                    :task-running (println status)
;;;                    (println status)))
;;;    (resourceOffers [driver offers]
;;;                    (doseq [offer offers]
;;;                      (let [uuid (str (java.util.UUID/randomUUID))]
;;;                        (when (&lt; 0 (:to-launch @scheduler-state))
;;;                          (try
;;;                            (mesos/launch-tasks driver (:id offer) (tasks-info uuid
;;;                                                                               offer))
;;;                            (println (tasks-info uuid offer))
;;;                            (swap! scheduler-state update-in [:to-launch] dec)
;;;                            (catch Exception e
;;;                              (.printStackTrace e)))))))))
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@

;; @@
