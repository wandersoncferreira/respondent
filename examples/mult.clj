(ns mult
  "core.async/mult is a way to broadcast information from one channel to many other channels."
  (:require [clojure.core.async :refer [mult go >! <! chan tap]]))

(def in (chan))
(def multiple (mult in))

(def out-1 (chan))
(tap multiple out-1)


(def out-2 (chan))
(tap multiple out-2)


(go (>! in "Single put!"))

(go (prn "Got from out-1 " (<! out-1)))
(go (prn "Got from out-2 " (<! out-2)))
