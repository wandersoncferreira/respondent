(ns respondent.core
  (:refer-clojure :exclude [filter map deliver take])
  (:require #?@(:clj [[clojure.core.async :as async
                       :refer [go go-loop chan <! >! timeout close! mult tap untap]]]
                :cljs [[cljs.core.async :as async
                        :refer [chan <! >! timeout close! mult tap untap]]]))
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))
  (:import
   #?@(:clj
       [(clojure.lang IDeref)])))


(defprotocol IBehavior
  (sample [b interval]
    "Turns this Behavior into an EventStream from the sampled values at the given interval"))

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
    "Returns true if this stream has stopped emitting values. False otherwise.")
  (take [s n]
    "Takes n items from the underlying event stream after which it will stop emitting items.")
  (zip [s es]
    "Zips together items emitted from two different event streams into a vector"))

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
  (take [_ n]
    (let [out (chan 1 (clojure.core/take n))]
      (tap multiple out)
      (event-stream out)))
  (zip [s es]
    (let [zipped-es (event-stream)
          out-s1 (chan)
          out-s2 (chan)]
      (tap multiple out-s1)
      (tap (.multiple es) out-s2)
      (go-loop []
        (let [item-s1 (<! out-s1)
              item-s2 (<! out-s2)
              zipped-value [item-s1 item-s2]]
          (when-not (empty? zipped-value)
            (deliver zipped-es zipped-value)
            (recur))))
      zipped-es))
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
  (#?(:clj deref :cljs -deref) [_] (f)))

(defn behavior
  [f]
  (Behavior. (fn [] (f))))


;;; Export functions to node library

#?(:cljs
   (defn generate-exports
     []
     #js {:event_stream event-stream
          :subscribe subscribe
          :map map
          :filter filter
          :take take
          :zip zip
          :flatmap flatmap
          :deliver deliver
          :completed? completed?
          :sample sample
          :dispose dispose
          :behavior behavior}))
