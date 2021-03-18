(ns cljdub-dsp.io
  "Namespace with functions for i/o operations and initialization of AudioSegment"
  (:require [cljdub-dsp.prop :refer [channels]]
            [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py..]])
  (:import (be.tarsos.transcoder Transcoder DefaultAttributes)))

(require-python '[pydub :bind-ns])
(require-python '[builtins :as bins])

(defn silent-seg
  "Creates a silent audiosegment, which can be used as a placeholder,
  spacer, or as a canvas to overlay other sounds on top of

  duration : int
    Length of the silent AudioSegment, in milliseconds"
  [duration]
  (py.. pydub -AudioSegment (silent :duration duration)))

(defn from-byte-string
  "Creates AudioSegment from python byte string"
  [byte-string & {:keys [sample-width sample-rate channels]}]
  (py.. pydub (**AudioSegment byte-string
                              {:sample_width sample-width
                               :frame_rate sample-rate
                               :channels channels})))

(defn from-bytes
  "Creates new AudioSegment from array of uint8 (unsigned bytes)"
  [bytes & {:keys [sample-width sample-rate channels]}]
  (-> bytes
      bins/bytes
      (from-byte-string :sample-width sample-width
                        :sample-rate sample-rate
                        :channels channels)))

(defn from-mono-segs
  "Creates new AudioSegment from several mono AudioSegments"
  [segs]
  {:pre [(every? #(-> % channels (= 1)) segs)]}
  (py.. pydub -AudioSegment (*from_mono_audiosegments segs)))

(defn load-seg
  "Loads AudioSegment from audio file

  path : str
    Path to the audio file"
  [path]
  (py.. pydub -AudioSegment (from_file path)))

(defn ->wav
  "Converts whatever audio to wav

  path-in : str
    Path to the input audio file

  path-out : str
    Path to the output audio file

  stereo = false : bool
    Whether convert to stereo"
  [path-in path-out & {:keys [stereo]
                       :or {stereo false}}]
  (Transcoder/transcode path-in path-out
                        (if stereo
                          DefaultAttributes/WAV_PCM_S16LE_STEREO_44KHZ
                          DefaultAttributes/WAV_PCM_S16LE_MONO_44KHZ))
  path-out)

(defn write-seg
  "Writes AudioSegment as a wav file

  path : str
    Path to the output file"
  [seg path]
  (py.. seg (export path :format "wav")))
