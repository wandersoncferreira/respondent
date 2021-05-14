(ns respondent.core
  (:refer-clojure :exclude [filter map deliver])
  (:require [respondent.behavior :refer [behavior]]
            [respondent.event-stream
             :refer
             [completed? deliver dispose event-stream filter flatmap map subscribe]]))

;;; Export functions to node library

#?(:cljs
   (defn generate-exports
     []
     #js {:event_stream event-stream
          :subscribe subscribe
          :map map
          :filter filter
          :flatmap flatmap
          :deliver deliver
          :completed? completed?
          :dispose dispose
          :behavior behavior}))
