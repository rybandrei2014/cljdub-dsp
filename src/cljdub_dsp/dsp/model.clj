(ns cljdub-dsp.dsp.model
  (:require [tech.v2.datatype :as dtype])
  (:import (be.tarsos.dsp AudioProcessor AudioDispatcher)
           (be.tarsos.dsp.io.jvm AudioDispatcherFactory)
           (javax.sound.sampled AudioFormat)
           (be.tarsos.dsp.resample Resampler)))

(defprotocol Factorable
  (setFactor [this factor]))

(deftype ParameterizedRateTransposer [factor ^Resampler resampler]
  AudioProcessor
  Factorable
  (setFactor [this factor]
    (reset! factor factor))
  (process [this audio-event]
    (let [src (.getFloatBuffer audio-event)
          src-size (count src)
          out-size (* src-size @factor)
          out (float-array out-size)]
      (.process resampler @factor src 0 src-size false out 0 out-size)
      (.setFloatBuffer audio-event out)
      (.setOverlap audio-event (* (.getOverlap audio-event)
                                  @factor))
      true))
  (processingFinished [this]))

(defn ->ParameterizedRateTransposer
  "RateTransposer parameterized with custom Resampler"
  [factor high-quality min-factor max-factor]
  (ParameterizedRateTransposer. (atom factor) (Resampler. high-quality min-factor max-factor)))

(deftype ChunkedBufferWriter [jvm-list-container offset]
  AudioProcessor
  (process [this event]
    (let [buffer (.getByteBuffer event)
          size (count buffer)
          new-size (+ size @offset)]
      (dtype/insert-block! jvm-list-container @offset buffer)
      (swap! offset (fn [off] (+ off size))))
    true)
  (processingFinished [this]))

(defn ->ChunkedBufferWriter
  "Audio processor that fills byte container (must be containter with extendable length, for example - list) with bytes from processing pipeline"
  [jvm-list-container offset]
  {:pre [(int? offset)
         (>= offset 0)]}
  (ChunkedBufferWriter. jvm-list-container (atom offset)))

(defrecord AudioDispatcherWrapper
    [dispatcher buffer-size buffer-overlap context])

(defn ->AudioDispatcherWrapper
  "Creates AudioDispatcherWrapper instance - record that contains AudioDispatcher instance with metadata information (unavailable in AudioDispatcher itself) - buffer size and buffer overlap. Also contains context which can be traversed by processors (atom with map)"
  [dispatcher buffer-size buffer-overlap context]
  {:pre [(instance? AudioDispatcher dispatcher)
         (int? buffer-size)
         (int? buffer-overlap)
         (> buffer-size buffer-overlap)
         (map? context)]}
  (AudioDispatcherWrapper. dispatcher buffer-size
                           buffer-overlap (atom context)))

(defn from-byte-array->AudioDispatcherWrapper
  "Creates AudioDispatcher from byte array with audio data and wraps the dispatcher in AudioDispatcherWrapper instance"
  [byte-array audio-format buffer-size buffer-overlap context]
  (->AudioDispatcherWrapper
   (AudioDispatcherFactory/fromByteArray
    byte-array audio-format buffer-size buffer-overlap)
   buffer-size
   buffer-overlap
   context))

(defn ->AudioFormat
  "Creates AudioFormat instance"
  [sample-rate sample-width channels is-signed is-big-endian]
  (AudioFormat. sample-rate sample-width channels is-signed is-big-endian))
