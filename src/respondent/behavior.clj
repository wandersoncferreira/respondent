(ns respondent.behavior
  (:refer-clojure :exclude [filter map deliver])
  (:require [clojure.core.async :refer [go-loop timeout <!]]
            [respondent.event-stream :refer [completed? event-stream deliver]])
  (:import [clojure.lang IDeref]))


(defprotocol IBehavior
  (sample [b interval]
    "Turns this Behavior into an EventStream from the sampled values at the given interval"))

;;; Behavior implementation

(defn from-interval
  "Creates and returns a new event stream which emits values at the given interval.

  If no other arguments are given, the values start at 0 and increment by one at each delivery.

  If given seed and succ it emits seed and applies succ to seed to
  get the next value. It then applies succ to the previous result and so on."
  ([msec]
   (from-interval msec 0 inc))
  ([msec seed succ]
   (let [es (event-stream)]
     (go-loop [timeout-ch (timeout msec)
               value seed]
       (when-not (completed? es)
         (<! timeout-ch)
         (deliver es value)
         (recur (timeout msec) (succ value))))
     es)))

(deftype Behavior [f]
  IBehavior
  (sample [_ interval]
    (from-interval interval (f) (fn [& args] (f))))
  IDeref
  (deref [_] (f)))

(defmacro behavior
  [& body]
  `(Behavior. #(do ~@body)))
