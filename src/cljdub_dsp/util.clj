(ns cljdub-dsp.util
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py..]]))

(require-python '[numpy :as np])

(defn numpy->bytes
  "Converts numpy object to its bytestring representation"
  [data]
  (py.. data (tobytes)))

(defn bytes->numpy
  "Creates numpy object from bytestring"
  [bytes]
  (np/fromstring bytes :dtype np/int8))

(defn empty-byte-numpy
  "Creates empty numpy object of size and dtype = int8"
  [size]
  (np/empty size :dtype np/int8))

(defn wrap-vec
  "Wrap in vector helper function, that works as follows:

  (wrap-vec 1 2 3 4) -> [1 2 3 4]
  (wrap-vec [1 2 3]) -> [1 2 3]
  (wrap-vec 1) -> [1]
  (wrap-vec '(1 2 3)) -> [1 2 3]
  (wrap-vec 1 2 3 [4 5 6]) -> [1 2 3 4 5 6]"
  ([value]
   (if (sequential? value)
      (into [] value)
      [value]))
  ([value & values]
   (concat (wrap-vec value) (apply wrap-vec values))))

(defn !nil?->
  "If not nil then else helper function, that works as follows:

  (!nil?-> nil 1 2) -> 2
  (!nil?-> nil 1) -> nil
  (!nil?-> 1 2 3) -> 2
  (!nil?-> 1 2) -> 2"
  ([value true-body]
   (if (every? some? (wrap-vec value)) true-body))
  ([value true-body false-body]
   (if (every? some? (wrap-vec value)) true-body false-body)))

(defmacro doto-when
  [this func val]
  `(when-let [unwraped-val# ~val]
     (~func ~this unwraped-val#)))

(defmacro doto-for
  ([this func values]
   `(doseq [elem# ~values]
      (~func ~this elem#)))
  ([this func value & rest]
   `(doto-for ~this ~func (list ~value ~@rest))))
