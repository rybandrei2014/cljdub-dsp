(ns cljdub-dsp.dsp.processor-test
  (:require [cljdub-dsp.dsp.processor :refer :all]
            [cljdub-dsp.dsp.interop :refer :all]
            [cljdub-dsp.operation :refer :all]
            [clojure.test :refer :all]
            [cljdub-dsp.prop-test :refer :all]))

(deftest apply-gain-test
  (let [seg (-> (test-seg) ->mono first)]
    (is (= (->> seg samples vec (map #(* % 2)))
           (-> seg
               (apply-processors {} (apply-gain 2))
               samples
               vec)))))

(deftest transpose-rate-test
  (let [seg (-> (test-seg) ->mono first)
        seg-samples-count (-> seg samples vec count)]
    (is (= (* 2 seg-samples-count)
           (-> seg
               (apply-processors {} (transpose-rate 2))
               samples
               vec
               count)))
    (is (= (int (* 0.5 seg-samples-count))
           (-> seg
               (apply-processors {} (transpose-rate 0.5))
               samples
               vec
               count)))))
