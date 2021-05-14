(ns exercise5-2
  (:require [respondent.core :as core]))

(def es1 (core/from-interval 3000))
(def es2 (core/map (core/from-interval 3000) #(* % 2)))
(def zipped (core/zip es1 es2))

(def token (core/subscribe zipped #(prn "Zipped values: " %)))

(core/dispose token)
