;; gorilla-repl.fileformat = 1

;; **
;;; # Part 2: Hello Mesos
;;;
;;; In this part, we'll get acqainted with the `hello-mesos` project better
;; **

;; **
;;; ## Initializing work from Part-1
;;;
;;;
;; **

;; @@
(ns stl-workshop.part-2
  (:require [user :refer :all]))
(reset! configuration {:mesos-master "mesos://127.0.0.1:5050"
                        :webui-port 5556
                        })

;; @@
