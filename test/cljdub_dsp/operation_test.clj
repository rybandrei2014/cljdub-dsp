(ns cljdub-dsp.operation-test
  (:require [cljdub-dsp.operation :refer :all]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.prop-test :refer :all]
            [clojure.test :refer :all]
            [libpython-clj.require :refer [require-python]]))

(require-python '[builtins :as bins])

(def operation-res
  {:dc-offset -1.52587890625E-4})

(deftest samples-test
  (is (= (-> (test-seg) samples bins/bytes)
         (-> audio-data bins/bytes))))

(deftest dc-offset-test
  (is (= (dc-offset (test-seg))
         (:dc-offset operation-res))))

(deftest slice-test
  ;; input = 5 ms length
  ;; slice input from  0 ms to 2 ms = 2 ms length
  (is (= (-> (test-seg) (slice :end 2) length)
         2))
  ;; slice input from 1 ms to 4 ms = 3 ms length
  (is (= (-> (test-seg) (slice :start 1 :end 4) length)
         3))
  ;; slice input from 4 ms to 5 ms = 1 ms length
  (is (= (-> (test-seg) (slice :start 4) length)
         1)))

(deftest split-test
  ;; input = 5 ms length
  ;; split input into 1 ms chunks = vector of 5 audio segments each 1 ms long
  (let [segs (-> (test-seg)
                 (split 1)
                 vec)]
    (is (= (count segs) 5))
    (is (every? #(= (length %) 1) segs)))
  ;; split input into 2 ms chunks = vector of 3 audio segments (2 ms long, 2 ms long, 1 ms long)
  (let [[f s t :as segs] (-> (test-seg)
                             (split 2)
                             vec)]
    (is (= (count segs) 3))
    (is (= (length f) 2))
    (is (= (length s) 2))
    (is (= (length t) 1)))
  ;; split input into 3 ms chunks = vector of 2 audio segments (3 ms long, 2 ms long)
  (let [[f s :as segs] (-> (test-seg)
                           (split 3)
                           vec)]
    (is (= (count segs) 2))
    (is (= (length f) 3))
    (is (= (length s) 2)))
  ;; slice from 2 ms to 5 ms and split input into 2 ms chunks = vector of 2 audio segments (2 ms long, 1 ms long)
  (let [[f s :as segs] (-> (test-seg)
                           (split 2 :start 2)
                           vec)]
    (is (= (count segs) 2))
    (is (= (length f) 2))
    (is (= (length s) 1)))
  ;; slice from 1 ms to 4 ms and split input into 1 ms chunks = vector of 3 audio segments (1 ms long, 1 ms long, 1 ms long)
  (let [[f s t :as segs] (-> (test-seg)
                             (split 1 :start 1 :end 4)
                             vec)]
    (is (= (count segs) 3))
    (is (every? #(= (length %) 1) segs))))

(deftest append-test
  (let [seg (test-seg)
        seg-1-2 (slice seg :end 2)
        seg-2-4 (slice seg :start 2 :end 4)
        seg-4-5 (slice seg :start 4)
        seg-3-5 (slice seg :start 3)]
    (is (= (-> seg length)
           (-> seg-1-2
               (append seg-2-4 :crossfade 0)
               (append seg-4-5 :crossfade 0)
               length)))
    (is (= (-> seg
               (append seg-1-2 :crossfade 0)
               (append seg-2-4 :crossfade 0)
               (append seg-4-5 :crossfade 0)
               (append seg-3-5 :crossfade 0)
               length)
           12))))

(deftest fn-frame-rate-test
  (let [seg (test-seg)
        f #(/ % 2)]
    (is (= (-> seg frame-rate f)
           (-> seg (fn-frame-rate f) frame-rate)))))

(deftest fn-sample-width-test
  (let [seg (test-seg)
        f #(/ % 2)]
    (is (= (-> seg sample-width f)
           (-> seg (fn-sample-width f) sample-width)))))

(deftest reversed-test
  (let [seg (test-seg)]
    (is (= (reverse (-> seg samples))
           (-> seg reversed samples vec)))))

(deftest repeated-test
  (let [seg (test-seg)]
    (is (= (-> seg (repeated 10) length)
           50))
    (is (= (-> seg (repeated 4) samples vec)
           (->> (samples seg)
                (repeat 4)
                (apply concat))))))

(deftest ->mono-test
  (let [mono-segs (-> (test-seg) ->mono vec)]
    (is (= (count mono-segs) 2))
    (is (every? #(= (channels %) 1) mono-segs))))
