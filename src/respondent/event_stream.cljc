(ns respondent.event-stream
  (:refer-clojure :exclude [filter map deliver])
  (:require #?@(:clj [[clojure.core.async :as async
                       :refer [go go-loop chan <! >! timeout close! mult tap untap]]]
                :cljs [[cljs.core.async :as async
                        :refer [chan <! >! timeout close! mult tap untap]]]))
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]])))


(defprotocol IEventStream
  (map [s f]
    "Returns a new stream containing the result of applying f to the values in s")
  (filter [s pred]
    "Returns a new stream containing the items from s for which pred returns true")
  (flatmap [s f]
    "Takes a function f from values in s to a new EventStream.
Returns an Event Stream containing values from all underlying streams combined.")
  (deliver [s value]
    "Delivers a value to the stream s")
  (completed? [s]
    "Returns true if this stream has stopped emitting values. False otherwise."))

(defprotocol IObservable
  (subscribe [obs f]
    "Register a callback to be invoked when the underlying source changes.
Returns a token the subscriber can use to cancel the subscription."))

(defprotocol IToken
  (dispose [tk]
    "Called when the subscriber isn't interested in receiving more items."))


;;; Token implementation

(deftype Token [ch]
  IToken
  (dispose [_]
    (close! ch)))


;;; Event Stream implementation

(declare event-stream)

(deftype EventStream [channel multiple completed]
  IEventStream
  (map [_ f]
    (let [out (chan 1 (clojure.core/map f))]
      (tap multiple out)
      (event-stream out)))
  (filter [_ pred]
    (let [out (chan 1 (clojure.core/filter pred))]
      (tap multiple out)
      (event-stream out)))
  (flatmap [_ f]
    (let [es (event-stream)
          out (chan)]
      (tap multiple out)
      (go-loop []
        (when-let [a (<! out)]
          (let [mb (f a)]
            (subscribe mb (fn [b] (deliver es b)))
            (recur))))
      es))
  (deliver [_ value]
    (if (= value ::complete)
      (do (reset! completed true)
          (go (>! channel value)
              (close! channel)))
      (go (>! channel value))))
  (completed? [_] @completed)

  IObservable
  (subscribe [this f]
    (let [out (chan)]
      (tap multiple out)
      (go-loop []
        (let [value (<! out)]
          (when (and value (not= value ::complete))
            (f value)
            (recur))))
      (Token. out))))


(defn event-stream
  "Created and returns a new event stream. You can optionally provide an
  existing core.async channel as the source for the new stream."
  ([]
   (event-stream (chan)))
  ([ch]
   (let [multiple (mult ch)
         completed (atom false)]
     (EventStream. ch multiple completed))))
