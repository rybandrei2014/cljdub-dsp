(ns cljdub-dsp.dsp.result-test
  (:require [cljdub-dsp.dsp.result :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [tech.v2.datatype :as dtype]
            [clojure.test.check.clojure-test :refer :all]))

(defspec byte-container->numpy-test
  100
  (prop/for-all [byte-container (gen/bind
                                 (gen/choose 1 100)
                                 (fn [c]
                                   (gen/bind
                                    (gen/vector
                                     (gen/choose -128 127) c)
                                    (fn [v]
                                      (gen/return
                                       (dtype/make-jvm-list :int8 v))))))]
                (= (vec byte-container)
                   (-> byte-container
                       byte-container->numpy
                       vec))))
