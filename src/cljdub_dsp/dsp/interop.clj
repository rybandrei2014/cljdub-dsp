(ns cljdub-dsp.dsp.interop
  "Namespace with functions for interoperation between TarsosDsp and
  pydub libraries"
  (:require [cljdub-dsp.operation :refer :all]
            [cljdub-dsp.dsp.result :refer :all]
            [cljdub-dsp.io :refer [from-mono-segs]]
            [cljdub-dsp.prop :refer :all]
            [cljdub-dsp.util :refer :all]
            [cljdub-dsp.dsp.model :refer :all]
            [tech.v2.datatype :as dtype]
            [libpython-clj.python :refer [py..]]))

(defn ->byte-array
  "Converts AudioSegment's data to java byte array"
  [seg]
  (let [data (bytes->numpy (raw-data seg))
        buffer (dtype/as-nio-buffer data)]
    (dtype/copy! buffer (byte-array (dtype/ecount buffer)))))

(defn apply-processors-gen
  "Generic function that creates TarsosDSP processing pipeline from passed processor/processors, that are transformed by passed transformer. The input SINGLE channel AudioSegment is processed by the pipeline and resulting data is returned as new AudioSegment

  opts : {:buffer-size int :buffer-overlap int}
    map with optional buffer-size - size of a buffer in TarsosDSP
    processing pipeline (default 2048), buffer-overlap - size of a buffer
    ovelap in TarsosDSP processing pipeline (default 0).

  result-fn : (AudioDispatcherWrapper, Jvm container with List<Byte>, AudioSegment) -> Result
    function that takes AudioDispatcherWrapper, Jvm container with byte data of resulting audio and input AudioSegment, returns result of function

  processor : AudioProcessor | [AudioProcessor]
    processor/processors which will be applied in TarsosDSP processing pipeline"
  ([seg opts result-fn processor]
   (let [byte-data (->byte-array seg)
         jvm-list (dtype/make-jvm-list :int8 0)
         sample-rate (frame-rate seg)
         width (* 8 (sample-width seg))
         channels (channels seg)
         audio-format (->AudioFormat sample-rate width channels true false)
         buffer-writer (fn [_] (->ChunkedBufferWriter jvm-list 0))
         dispatcher-wrapper (from-byte-array->AudioDispatcherWrapper
                             byte-data
                             audio-format
                             (or (:buffer-size opts) 2048)
                             (or (:buffer-overlap opts) 0)
                             (or (:context opts) {}))]
     (doto (:dispatcher dispatcher-wrapper)
       (doto-for .addAudioProcessor
                 (flatten (map #(% dispatcher-wrapper)
                               (wrap-vec processor buffer-writer))))
       (.run))
     (result-fn dispatcher-wrapper jvm-list seg)))
  ([seg opts result-fn processor & processors]
   (apply-processors-gen seg opts result-fn
                         (apply wrap-vec processor processors))))

(defn apply-processors
  "Same as apply-processors-gen but using seg-result result-fn"
  [seg opts processor & processors]
  (apply-processors-gen seg opts seg-result
                        (apply wrap-vec processor processors)))

(defn apply-processors-multi-channel-gen
  "Same as apply-processors-gen only used to process MULTI-CHANNEL AudioSegment"
  ([seg opts result-fn processor & processors]
   (if (> (channels seg) 1)
     (->> seg
         ->mono
         (map #(apply-processors-gen
                % opts result-fn
                (apply wrap-vec processor processors)))
         from-mono-segs)
     (throw (IllegalArgumentException.
             "seg with multi-channel audio must be provided")))))

(defn apply-processors-multi-channel
  "Same as apply-processors only used to process MULTI-CHANNEL Audio-Segment"
  ([seg opts processor & processors]
   (apply apply-processors-multi-channel-gen
          seg opts seg-result
          processor processors)))
