(ns cljdub-dsp.operation
  "Namespace with functions for basic pydub operations on AudioSegment"
  (:require [cljdub-dsp.prop :refer :all]
            [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py.. get-item]]))

(require-python '[builtins]
                '[pydub.playback])

(defn play
  "Plays audio segment"
  [seg]
  (pydub.playback/play seg))

(defn samples
  "Returns the raw audio data as an array of (numeric) samples.
  Note: if the audio has multiple channels,
  the samples for each channel will be serialized – for example:
  stereo audio would look like
  [sample_1_L, sample_1_R, sample_2_L, sample_2_R, …].

  seg : AudioSegment"
  [seg]
  (py.. seg (get_array_of_samples)))

(defn dc-offset
  "Returns a value between -1.0 and 1.0 representing
  the DC offset of a channel.
  This is calculated using audioop.avg() and
  normalizing the result by samples max value."
  [seg]
  (py.. seg (get_dc_offset)))

(defn remove-dc-offset
  "Removes DC offset from channel(s).
  This is done by using audioop.bias(), so watch out for OVERFLOWS!"
  [seg]
  (py.. seg (remove_dc_offset)))

(defn spawn
  "Spawns AudioSegment with new audio data in form of byte string or python array"
  [seg samples]
  (py.. seg (_spawn samples)))

(defn slice
  "Slices the AudioSegment into new AudioSegment

  start = nil : int
    Begin index of a resulting slice

  end = nil : int
    End index of a resulting slice"
  [seg & {:keys [start end]
          :or {start nil, end nil}}]
  (get-item seg (builtins/slice start end)))

(defn split
  "Splits AudioSegment into AudioSegments with slices

  duration : int
    Size of a slice in samples

  start = nil : int
    Begin index from where to split

  end = nil : int
    End index where to stop splitting"
  [seg duration & {:keys [start end]
                   :or {start nil, end nil}}]
  (get-item seg (builtins/slice start end duration)))

(defn overlay
  "Overlays an AudioSegment onto this one.
  In the resulting AudioSegment they will play simultaneously.
  If the overlaid AudioSegment is longer than this one,
  the result will be truncated (so the end of the overlaid sound will be cut off).
  The result is always the same length as this AudioSegment
  even when using the loop, and times keyword arguments.

  left-seg : AudioSegment
    Seg to be overlayed

  right-seg : AudioSegment
    Seg used as overlay

  position : int
    Index where to start overlaying

  loop : bool
    Loop seg as many times as necessary to match this segment's length

  times : int
    Loop seg the specified number of times or until it matches this segment's length

  overlay-gain : int
    Changes this segment's volume by the specified amount
    during the duration of time that seg is overlaid on top of it.
    When negative, this has the effect of 'ducking' the audio under the overlay"
  [left-seg right-seg & {:keys [position loop times overlay-gain]
                         :or {position 0, loop false,
                              times nil, overlay-gain nil}}]
  (py.. left-seg
        (overlay right-seg
                 :position position :loop loop
                 :times times :gain_during_overlay overlay-gain)))

(defn append
  "Returns a new AudioSegment, created by appending
  another AudioSegment to this one (i.e., adding it to the end)

  left-seg : AudioSegment

  right-seg : AudioSegment

  crossfade = 100 : int"
  [left-seg right-seg & {:keys [crossfade]
                         :or {crossfade 100}}]
  (py.. left-seg (append right-seg :crossfade crossfade)))

(defn gain
  "Change the amplitude (generally, loudness) of the AudioSegment.
  Gain is specified in dB

  amount : int"
  [seg amount]
  (py.. seg (apply_gain amount)))

(defn stereo-gain
  "Apply gain to the left and right channel of a stereo AudioSegment.
  If the AudioSegment is mono, it will be converted to stereo
  before applying the gain.

  left-gain : int

  right-gain : int"
  [seg left-gain right-gain]
  (py.. seg (apply_gain_stereo left-gain right-gain)))

(defn fn-frame-rate
  "Creates an equivalent version of this AudioSegment
  with new frame rate retrived by passed func which
  gets original frame rate and returns new one

  f : func{int -> int}"
  [seg f]
  (py.. seg (set_frame_rate (-> seg frame-rate f))))

(defn fn-sample-width
  "Creates an equivalent version of this AudioSegment
  with new sample width retrived by passed func which
  gets original sample width and returns new one

  f : func{int -> int}"
  [seg f]
  (py.. seg (set_sample_width (-> seg sample-width f))))

(defn fade
  "Applies fade effect to the AudioSegment

  start : int
    Position to begin fading (in milliseconds)

  end : int
    Position to stop fading (in milliseconds)

  from-gain : int
    Change at the beginning of the fade (in dBs)

  to-gain : int
    Resulting change at the end of the fade"
  [seg start end & {:keys [from-gain to-gain]
                    :or {from-gain 0, to-gain 0}}]
  (py.. seg (fade :start start :end end
                  :from_gain from-gain :to_gain to-gain)))

(defn fade-out
  "Fade out (to silent) the end of the AudioSegment

  duration : int
    How long (in milliseconds) the fade should last"
  [seg duration]
  (py.. seg (fade_out duration)))

(defn fade-in
  "Fade in (from silent) the beginning of the AudioSegment

  duration : int
    How long (in milliseconds) the fade should last"
  [seg duration]
  (py.. seg (fade_in duration)))

(defn reversed
  "Creates new AudioSegment from input that plays backwards"
  [seg]
  (py.. seg (reverse)))

(defn repeated
  "Repeats audio n times and returns new AudioSegment with repeated audio

  times : int
    Number of repeats"
  [seg times]
  (py.. seg (__mul__ times)))

(defn ->mono
  "Splits AudioSegment into mono ones:
  - if input seg is stereo, the result will be sequence of two segs with 2 mono channels
  - if input seg is mono, nothing is done, just simply return input seg"
  [seg]
  (py.. seg (split_to_mono)))

(defn chunkify
  "Splits AudioSegment into chunks and applies AudioSegment transforming function on each chuck, after that chunks are joined in order they were originally splitted. Note: can help improve performace especially on TarsosDSP AudioSegment processing functions - example: apply-processors, apply-processors-multi-channel ...

  f : function (AudioSegment) -> (AudioSegment)
    AudioSegment transforming function"
  [seg f & {:keys [chunk-size crossfade start end]
            :or {chunk-size 9000
                 crossfade 0
                 start nil
                 end nil}}]
  (let [[head & tail] (split seg chunk-size :start start :end end)]
    (reduce #(append %1 (f %2) :crossfade crossfade)
            (f head)
            tail)))
