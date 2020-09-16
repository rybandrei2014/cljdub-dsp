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

(defmacro doto-for
  "Macro that works similar to doto, but simply apply single function f through on provided values thus mutating input object (this). For example: (doto-for (new java.util.ArrayList) .add 1 2 3) does following [].add(1).add(2).add(3) => [1 2 3]"
  ([this f vals]
   (let [res (gensym)]
     `(let [~res ~this]
        (doseq [val# ~vals]
            (~f ~res val#))
        ~res)))
  ([this f val & rest]
   `(doto-for ~this ~f (list ~val ~@rest))))
