(ns cljdub-dsp.dsp.processor
  "Namespace with functions that take AudioDispatcherWrapper and return AudioProcessor"
  (:require [cljdub-dsp.util :refer :all]
            [cljdub-dsp.dsp.model :refer [->ParameterizedRateTransposer]])
  (:import (be.tarsos.dsp AudioProcessor)
           (be.tarsos.dsp.synthesis AmplitudeLFO NoiseGenerator
                                    SineGenerator)
           (be.tarsos.dsp.effects DelayEffect FlangerEffect)
           (be.tarsos.dsp BitDepthProcessor PitchShifter AutoCorrelation
                          AudioProcessor WaveformSimilarityBasedOverlapAdd
                          WaveformSimilarityBasedOverlapAdd$Parameters
                          GainProcessor)
           (be.tarsos.dsp.resample Resampler)))

(defn- wsola-param
  [sample-rate tempo param & {:keys [sequenceMs seekWindowMs overlapMs]
                              :or {sequenceMs 82
                                   seekWindowMs 28
                                   overlapMs 12}}]
  (if (some? param)
   (or (param
        {:music
         (WaveformSimilarityBasedOverlapAdd$Parameters/musicDefaults
          tempo sample-rate)
         :slowdown
         (WaveformSimilarityBasedOverlapAdd$Parameters/slowdownDefaults
          tempo sample-rate)
         :speech
         (WaveformSimilarityBasedOverlapAdd$Parameters/speechDefaults
          tempo sample-rate)
         :automatic
         (WaveformSimilarityBasedOverlapAdd$Parameters/automaticDefaults
          tempo sample-rate)})
       (throw
        (IllegalArgumentException.
         "valid keyword for wsola parameter must be provided")))
   (WaveformSimilarityBasedOverlapAdd$Parameters. tempo
                                                  sample-rate
                                                  sequenceMs
                                                  seekWindowMs
                                                  overlapMs)))

(defn transpose-rate
  "Sample rate transposer. Changes sample rate by using interpolation
  Together with the time stretcher this can be used for pitch shifting."
  [rate & {:keys [high-quality min-factor max-factor]
           :or {high-quality false, min-factor 0.1, max-factor 10.0}}]
  (fn [_]
    (->ParameterizedRateTransposer rate high-quality min-factor
                                   max-factor)))

(defn apply-gain
  "Apply gain processor"
  [gain]
  (fn [_] (GainProcessor. gain)))

(defn generate-noise
  "Processor that adds noise to the audio data"
  [gain]
  (fn [_] (NoiseGenerator. gain)))

(defn generate-sine
  "Processor that adds generated sine wave to the audio data"
  [gain freq]
  (fn [_] (SineGenerator. gain freq)))

(defn amplitude-lfo
  "Processor that applies low frequency oscilation to the audio data"
  [freq scale]
  (fn [_] (AmplitudeLFO. freq scale)))

(defn flanger-effect
  "Processor that adds a flanger effect to a signal.
  The implementation is done with a delay
  buffer and an LFO in the form of a sine wave. It is probably the most
  straightforward flanger implementation possible."
  [sample-rate max-flanger-length wet freq]
  (fn [_] (FlangerEffect. max-flanger-length wet sample-rate freq)))

(defn delay-effect
  "Adds an echo effect to the signal.

  sample-rate : int
    audio sample rate in Hz

  echo-length : int
    echo length in seconds

  decay : float
    decay of the echo, a value between 0 and 1. 1 meaning no decay,
    0 means immediate decay (not echo effect)"
  [sample-rate echo-length decay]
  (fn [_] (DelayEffect. echo-length decay sample-rate)))

(defn bit-depth
  "Processor that simply transforms every sample to the requested bit depth."
  [depth]
  (fn [_] (doto (BitDepthProcessor.)
            (.setBitDepth depth))))

(defn wsola-processor
  [factor & {:keys [type sequenceMs seekWindowMs overlapMs]
            :or {type :slowdown
                 sequenceMs 82
                 seekWindowMs 28
                 overlapMs 12}}]
  (fn [wrapper]
    (let [dispatcher (:dispatcher wrapper)
          sample-rate (-> dispatcher .getFormat .getSampleRate)
          tempo (/ 1 factor)
          wsola (WaveformSimilarityBasedOverlapAdd.
                 (wsola-param sample-rate tempo type
                              :sequenceMs sequenceMs
                              :seekWindowMs seekWindowMs
                              :overlapMs overlapMs))
          wsola-buffer-size (.getInputBufferSize wsola)
          wsola-buffer-overlap (.getOverlap wsola)]
      (.setStepSizeAndOverlap dispatcher
                              wsola-buffer-size
                              wsola-buffer-overlap)
      (.setDispatcher wsola dispatcher)
      wsola)))
