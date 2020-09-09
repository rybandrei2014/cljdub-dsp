(ns cljdub-dsp.prop
  "Namespace with functions that return properties of AudioSegment"
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py..]]))

(require-python '[builtins :refer [len]])

(defn dBFS
  "Returns the loudness of the AudioSegment in dBFS
  (db relative to the maximum possible loudness)"
  [seg]
  (py.. seg -dBFS))

(defn channels
  "Returns number of channels in this audio segment"
  [seg]
  (py.. seg -channels))

(defn sample-width
  "Returns number of bytes in each sample (1 means 8 bit, 2 means 16 bit, etc)"
  [seg]
  (py.. seg -sample_width))

(defn frame-rate
  "Returns frame rate of audio"
  [seg]
  (py.. seg -frame_rate))

(defn max-amp
  "Returns the highest amplitude of any sample in the AudioSegment"
  [seg]
  (py.. seg -max))

(defn rms-amp
  [seg]
  (py.. seg -rms))

(defn max-possible-amp
  [seg]
  (py.. seg -max_possible_amplitude))

(defn max-dBFS
  "Returns the highest amplitude of any sample in the AudioSegment,
  in dBFS (relative to the highest possible amplitude value)"
  [seg]
  (py.. seg -max_dBFS))

(defn raw-data
  "Returns the raw audio data of the AudioSegment as a bytestring"
  [seg]
  (py.. seg -raw_data))

(defn frame-count
  "Returns the number of frames in the AudioSegment"
  [seg]
  (py.. seg (frame_count)))

(defn length
  "Returns length of AudioSegment in milliseconds"
  [seg]
  (len seg))

(defn array-type
  [seg]
  (py.. seg -array_type))

(defn seg-props
  "Returns a map with AudioSegment properties"
  [seg]
  {:dBFS (dBFS seg)
   :max-dBFS (max-dBFS seg)
   :channels (channels seg)
   :sample-width (sample-width seg)
   :frame-rate (frame-rate seg)
   :max-amp (max-amp seg)
   :rms-amp (rms-amp seg)
   :max-possible-amp (max-possible-amp seg)
   :frame-count (frame-count seg)
   :length (length seg)})
