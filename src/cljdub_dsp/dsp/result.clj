(ns cljdub-dsp.dsp.result
  "Namespace with so called TarsosDSP processing result functions - function that returns result of TarsosDSP processing pipeline. Every function take AudioDispatcherWrapper, Jvm container with byte data of resulting audio and input AudioSegment, returns generic result of TarsosDSP processing pipeline"
  (:require [cljdub-dsp.operation :refer :all]
            [cljdub-dsp.util :refer :all]
            [tech.v2.datatype :as dtype]))

(defn byte-container->numpy
  "Fills numpy array with bytes from byte container"
  [container]
  (let [size (dtype/ecount container)
        numpy-data (empty-byte-numpy size)
        buffer (dtype/as-nio-buffer numpy-data)]
    (dtype/copy! container 0 buffer 0 size)
    numpy-data))

(defn seg-result
  "Take byte data from resulting Jvm container and spawns input AudioSegment with that data, thus returning AudioSegment with new data"
  [wrapper jvm-list seg]
  (->> jvm-list
       byte-container->numpy
       numpy->bytes
       (spawn seg)))
