;; gorilla-repl.fileformat = 1

;; **
;;; # Part 1: Getting Started with Clj-Mesos
;;; 
;;; Hey there! Welcome to the StrangeLoop 2015 Workshop on "Building Distributed Frameworks using Clojure and Mesos." We hope you learn a lot in the workshop and have fun! 
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
