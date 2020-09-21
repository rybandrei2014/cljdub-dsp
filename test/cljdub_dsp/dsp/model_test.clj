(ns cljdub-dsp.dsp.model-test
  (:require [cljdub-dsp.dsp.model :refer :all]
            [cljdub-dsp.util :refer :all]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.operation :refer :all]
            [cljdub-dsp.io :refer :all]
            [cljdub-dsp.dsp.result :refer [byte-container->numpy]]
            [cljdub-dsp.dsp.interop :refer [->byte-array]]
            [clojure.test :refer :all]
            [cljdub-dsp.prop-test :refer :all]
            [tech.v2.datatype :as dtype]))

(deftest ChunkedBufferWriter-test
  (let [seg (-> (test-seg) ->mono first)
        byte-data (->byte-array seg)
        width (* 8 (sample-width seg))
        channels (channels seg)
        audio-format (->AudioFormat 44100
                                    width
                                    channels
                                    true
                                    false)]
    ; test that input byte data is the same as output one
    (let [jvm-list (dtype/make-jvm-list :int8 0)
          buffer-writer (->ChunkedBufferWriter jvm-list 0)
          dispatcher (:dispatcher (from-byte-array->AudioDispatcherWrapper
                                   byte-data
                                   audio-format
                                   2
                                   0
                                   {}))]
      (doto dispatcher
        (.setZeroPadLastBuffer false)
        (.addAudioProcessor buffer-writer)
        .run)
      ;; test that byte data in input AudioSegment is the same as byte content of jvm list container, which was filled by ChunkedBufferWriter
      (is (= (vec byte-data)
             (vec jvm-list)))
      ;; test that raw audio data of input AudioSegment is the same as raw audio data of output AudioSegment, which audio data has been populated from jvm list container, which was filled by ChunkedBufferWriter
      (is (= (-> seg samples vec)
             (->> (-> jvm-list
                      byte-container->numpy
                      numpy->bytes)
                  (spawn seg)
                  samples
                  vec))))
    (let [jvm-list (dtype/make-jvm-list :int8 0)
          buffer-writer (->ChunkedBufferWriter jvm-list 1)
          dispatcher (:dispatcher (from-byte-array->AudioDispatcherWrapper
                                   byte-data
                                   audio-format
                                   1
                                   0
                                   {}))]
      (doto dispatcher
        (.setZeroPadLastBuffer false)
        (.addAudioProcessor buffer-writer)
        .run)
      ;; ChunkedBufferWriter has been offsetted by 1 -> output jvm list container will have all bytes from input except first one
      (is (= (vec byte-data)
             (-> jvm-list vec rest))))
    (let [jvm-list (dtype/make-jvm-list :int8 0)
          buffer-writer (->ChunkedBufferWriter jvm-list 0)
          dispatcher (:dispatcher (from-byte-array->AudioDispatcherWrapper
                                   byte-data
                                   audio-format
                                   2
                                   1
                                   {}))
          [byte-data-partitioned-head & byte-data-partitioned-rest]
          (->> byte-data vec (partition 2))]
      (doto dispatcher
        (.setZeroPadLastBuffer false)
        (.addAudioProcessor buffer-writer)
        .run)
      ;; buffer overlap == buffer size / 2: input [1 1 0 0 2 1 7 8 3 5] -> output [1 1 0 0 0 0 2 1 2 1 7 8 7 8 3 5]
      (is (= (apply concat
                    (conj (vec
                           (cons byte-data-partitioned-head
                                 (mapcat
                                  (partial repeat 2)
                                  (butlast byte-data-partitioned-rest))))
                          (last byte-data-partitioned-rest)))
             (vec jvm-list))))))
