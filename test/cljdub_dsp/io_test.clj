(ns cljdub-dsp.io-test
  (:require [cljdub-dsp.prop-test :refer :all]
            [clojure.test :refer :all]
            [cljdub-dsp.io :refer :all]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.operation :refer :all]
            [libpython-clj.require :refer [require-python]]))

(require-python '[builtins :as bins])

(deftest silent-seg-test
  (let [seg (silent-seg 1)]
    (is (= (length seg)
           1))
    (is (every? #(= % 0) (samples seg)))))

(deftest from-bytes-test
  (let [byte-string (bins/bytes audio-data)
        seg (from-bytes audio-data
                        :sample-width audio-sample-width
                        :sample-rate audio-frame-rate
                        :channels audio-channels)]
    (is (= (raw-data seg)
           byte-string))
    (is (= (sample-width seg)
           audio-sample-width))
    (is (= (frame-rate seg)
           audio-frame-rate))
    (is (= (channels seg)
           audio-channels))))

(deftest from-byte-string-test
  (let [byte-string (bins/bytes audio-data)
        seg (from-byte-string byte-string
                              :sample-width audio-sample-width
                              :sample-rate audio-frame-rate
                              :channels audio-channels)]
    (is (= (raw-data seg)
           byte-string))
    (is (= (sample-width seg)
           audio-sample-width))
    (is (= (frame-rate seg)
           audio-frame-rate))
    (is (= (channels seg)
           audio-channels))))

(deftest from-mono-segs-test
  (let [[f :as segs] (-> (test-seg) ->mono vec)]
    (is (= (-> segs from-mono-segs channels)
           2))
    (is (= (-> (cons f segs) from-mono-segs channels)
           3))))
