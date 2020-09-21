(ns cljdub-dsp.dsp.interop-test
  (:require [clojure.test :refer :all]
            [cljdub-dsp.prop-test :refer :all]
            [cljdub-dsp.dsp.interop :refer :all]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.util :refer :all]
            [cljdub-dsp.operation :refer :all]
            [cljdub-dsp.dsp.processor :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]))

(deftest ->byte-array-test
  (let [seg (test-seg)
        byte-numpy (-> seg raw-data bytes->numpy)
        byte-array (->byte-array seg)]
    (is (= (vec byte-numpy)
           (vec byte-array)))))

(defspec apply-processors-gen-test
  ;; generative test for tarsosdsp processing pipeline
  50
  (prop/for-all [processors (gen/vector-distinct
                             (gen/elements
                              [(transpose-rate 1.2)
                               (apply-gain 1.2)
                               (generate-noise 1.2)
                               (generate-sine 1.2 200)
                               (amplitude-lfo 200 1.2)
                               (flanger-effect 44100 1 0.5 200)
                               (delay-effect 44100 1 0.9)
                               (bit-depth 8)])
                             {:min-elements 1 :max-elements 8})]
                (apply apply-processors-gen
                       (-> (test-seg) ->mono first)
                       {}
                       (fn [d jvm seg] jvm)
                       processors)))

(deftest apply-processors-multi-channel-test
  (let [seg (-> (test-seg) ->mono first)]
    ;; only multi channel AudioSegment must be accepted
    (is (thrown?
         java.lang.IllegalArgumentException
         (apply-processors-multi-channel seg {} (apply-gain 1))))))
