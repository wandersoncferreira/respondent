(ns event-stream
  (:require [respondent.core :as core]))

(def es1 (core/event-stream))

(core/subscribe es1 #(prn "first event stream emitted: " %))
(core/deliver es1 10)

(def es2 (core/map es1 #(* 2 %)))
(core/subscribe es2 #(prn "second event stream emitted: " %))
(core/deliver es1 20)


;;; time behavior
(def time-behavior (core/behavior (System/nanoTime)))

(def time-stream (core/sample time-behavior 1500))

(def token (core/subscribe time-stream #(prn "Time is " %)))

(core/dispose token)
