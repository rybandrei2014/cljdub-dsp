(ns cljdub-dsp.dsp.pitch
  "Namespace with functions for initialization of PitchProcessor with custom PitchDetectionHandler"
  (:import (be.tarsos.dsp.pitch PitchProcessor
                                PitchProcessor$PitchEstimationAlgorithm
                                PitchDetectionHandler)
           (be.tarsos.dsp.synthesis PitchResyntheziser)))

(defn- pitch-estimation-algorithm
  "Returns pitch estimation algorithm by key, otherwise throws IllegalArgumentException"
  [algorithm]
  (or (algorithm
       {:fft-yin PitchProcessor$PitchEstimationAlgorithm/FFT_YIN
        :yin PitchProcessor$PitchEstimationAlgorithm/YIN
        :amdf PitchProcessor$PitchEstimationAlgorithm/AMDF
        :mpm PitchProcessor$PitchEstimationAlgorithm/MPM
        :dynamic-wavelet
        PitchProcessor$PitchEstimationAlgorithm/DYNAMIC_WAVELET
        :fft-pitch PitchProcessor$PitchEstimationAlgorithm/FFT_PITCH})
      (throw
       (IllegalArgumentException.
        "valid keyword for pitch estimation algorithm must be provided"))))

(defn pitch-processor
  "Creates PitchProcessor with custom PitchDetectionHandler"
  [sample-rate pitch-detection-handler algorithm]
  (fn [wrapper]
    (PitchProcessor. (pitch-estimation-algorithm algorithm)
                     sample-rate
                     (:buffer-size wrapper)
                     pitch-detection-handler)))

(defn pitch-resyntheziser
  "Creates PitchProcessor with pitch detection handler that replaces the audio buffer in the pipeline with a synthesized wave. It either follows the envelope of the original signal or not."
  [sample-rate algorithm
   & {:keys [follow-envelope pure-sine filter-size]
      :or {follow-envelope true, pure-sine false, filter-size 5}}]
  (pitch-processor sample-rate
                   (PitchResyntheziser. sample-rate follow-envelope
                                        pure-sine filter-size)
                   algorithm))
