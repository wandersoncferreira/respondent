(ns respondent.core
  (:refer-clojure :exclude [filter map deliver])
  (:require #?@(:clj [[clojure.core.async :as async
                       :refer [go go-loop chan <! >! timeout
                               map> filter> close! mult tap untap]]]
                :cljs [[cljs.core.async :as async
                        :refer [chan <! >! timeout map> filter>
                                close! mult tap untap]]]))
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:import
   #?@(:clj
       [(clojure.lang IDeref)])))


(defn teste
  []
  (println "RESTADO"))


#?(:cljs
   (defn generate-exports
     []
     #js {:teste teste}))
