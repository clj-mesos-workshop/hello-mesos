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
