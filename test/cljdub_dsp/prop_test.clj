(ns cljdub-dsp.prop-test
  (:require [clojure.test :refer :all]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.io :refer [from-byte-string]]
            [libpython-clj.require :refer [require-python]]))

(require-python '[builtins :as bins])

(def audio-data
  [1 0 1 0 0 0 0 0 254 255 0 0 0 0 1 0 255 255 1 0 252 255 255 255 253 255 0 0 253 255 0 0 252 255 254 255 255 255 1 0 254 255 0 0 254 255 0 0 0 0 1 0 0 0 1 0 2 0 2 0 3 0 2 0 3 0 2 0 5 0 4 0 6 0 3 0 7 0 5 0 7 0 6 0 5 0 4 0 7 0 6 0 6 0 6 0 4 0 4 0 8 0 7 0 9 0 8 0 9 0 7 0 9 0 7 0 7 0 5 0 5 0 5 0 4 0 5 0 5 0 7 0 7 0 5 0 3 0 2 0 2 0 2 0 253 255 255 255 252 255 254 255 254 255 255 255 253 255 253 255 255 255 0 0 254 255 254 255 0 0 254 255 0 0 0 0 255 255 253 255 0 0 0 0 0 0 0 0 255 255 0 0 253 255 255 255 253 255 253 255 254 255 253 255 0 0 255 255 250 255 250 255 246 255 247 255 253 255 254 255 0 0 0 0 252 255 254 255 253 255 254 255 252 255 253 255 255 255 255 255 254 255 254 255 252 255 254 255 251 255 254 255 252 255 254 255 0 0 0 0 253 255 252 255 254 255 253 255 0 0 255 255 253 255 252 255 254 255 254 255 253 255 253 255 0 0 0 0 254 255 255 255 250 255 250 255 254 255 254 255 251 255 251 255 252 255 252 255 254 255 254 255 253 255 253 255 255 255 255 255 255 255 255 255 248 255 248 255 245 255 245 255 0 0 254 255 250 255 249 255 249 255 249 255 255 255 255 255 251 255 252 255 255 255 0 0 252 255 253 255 249 255 249 255 255 255 253 255 251 255 250 255 252 255 252 255 252 255 252 255 248 255 250 255 247 255 249 255 249 255 250 255 249 255 249 255 248 255 248 255 248 255 249 255 251 255 251 255 0 0 0 0 253 255 254 255 252 255 252 255 1 0 0 0 248 255 247 255 0 0 254 255 1 0 0 0 249 255 249 255 0 0 0 0 0 0 0 0 253 255 254 255 255 255 0 0 254 255 254 255 255 255 255 255 0 0 255 255 0 0 0 0 255 255 255 255 255 255 255 255 253 255 253 255 253 255 253 255 2 0 0 0 255 255 254 255 0 0 0 0 0 0 0 0 254 255 254 255 253 255 253 255 253 255 253 255 0 0 255 255 250 255 249 255 252 255 251 255 254 255 253 255 246 255 246 255 248 255 248 255 249 255 249 255 250 255 250 255 248 255 249 255 246 255 246 255 252 255 252 255 249 255 251 255 250 255 251 255 250 255 251 255 249 255 250 255 249 255 250 255 249 255 249 255 252 255 251 255 249 255 248 255 251 255 251 255 251 255 251 255 251 255 251 255 252 255 252 255 248 255 249 255 250 255 251 255 248 255 248 255 246 255 246 255 251 255 250 255 250 255 247 255 250 255 249 255 246 255 246 255 246 255 246 255 250 255 250 255 248 255 248 255 250 255 249 255 247 255 247 255 247 255 246 255 250 255 249 255 250 255 250 255 252 255 251 255 250 255 249 255 251 255 250 255 249 255 248 255 248 255 247 255 250 255 250 255 249 255 249 255 247 255 249 255 249 255 250 255 251 255 251 255 247 255 248 255 245 255 246 255 249 255 249 255 247 255 247 255 248 255 249 255 248 255 249 255 246 255 247 255 246 255 248 255 246 255 247 255 248 255 249 255 248 255 248 255 245 255 245 255 248 255 247 255 248 255 248 255 244 255 244 255 245 255 246 255 247 255 248 255 246 255 246 255 247 255 248 255 248 255 248 255 244 255 245 255 244 255 245 255 247 255 247 255 247 255 247 255 243 255 244 255 245 255 246 255 247 255 248 255 246 255 246 255 247 255 248 255 245 255 246 255 244 255 246 255 244 255 246 255 244 255 246 255 247 255 248 255 245 255 247 255 246 255 247 255 246 255 249 255 244 255 247 255 244 255 247 255 246 255 249 255 246 255 248 255 245 255 247 255])

(def audio-frame-rate 44100)

(def audio-sample-width 2)

(def audio-channels 2)

(defn test-seg
  []
  (-> audio-data bins/bytes (from-byte-string audio-sample-width
                                              audio-frame-rate
                                              audio-channels)))

(def test-seg-props
  {:max-possible-amp 32768.0,
   :frame-rate 44100,
   :channels 2,
   :sample-width 2,
   :max-dBFS -68.03013165305762,
   :max-amp 13,
   :rms-amp 6,
   :length 5,
   :dBFS -74.74597369152148,
   :frame-count 220.0
   :array-type "h"})

(defmacro prop-tests
  [& props]
  `(do
     ~@(for [prop props]
         `(deftest ~(symbol (str prop "-test"))
            (is (= (~prop (test-seg))
                   (~(keyword prop) test-seg-props)))))))

(prop-tests dBFS channels sample-width frame-rate max-amp rms-amp
            max-possible-amp max-dBFS frame-count length array-type)