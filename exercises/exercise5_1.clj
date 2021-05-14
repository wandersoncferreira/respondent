(ns exercise5-1
  (:require [respondent.core :as core]))

(def es1 (core/from-interval 30))
(def take-es (core/take es1 5))

(core/subscribe take-es #(prn "Take values: " %))
