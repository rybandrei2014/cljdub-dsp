(ns cljdub-dsp.effect
  "Namespace with functions that apply pydub effects on AudioSegment"
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py..]]
            [cljdub-dsp.operation :refer [samples]]
            [cljdub-dsp.prop :refer [frame-rate]]))

;; require python modules
(require-python '[pydub.scipy_effects])
(require-python '[pydub.silence :refer [split_on_silence]])

(defn invert-phase
  "invert the phase of the signal
  Note that mono audio seg will become stereo."
  [seg]
  (py.. seg (invert_phase)))

(defn normalize
  "headroom = 0.1
     how close to the maximum volume to boost the signal up to (specified in dB)"
  [seg & {:keys [headroom] :or {headroom 0.1}}]
  (py.. seg (normalize :headroom headroom)))

(defn speed-up
  [seg speed & {:keys [chunk-size crossfade]
                :or {chunk-size 150, crossfade 25}}]
  (py.. seg (speedup speed
                     :chunk_size chunk-size
                     :crossfade crossfade)))

(defn strip-silence
  "removes the silence from audio seg"
  [seg & {:keys [silence-len silence-thresh padding]
          :or {silence-len 1000, silence-thresh -30, padding 100}}]
  (py.. seg (strip_silence :silence_len silence-len
                           :silence_thresh silence-thresh
                           :padding padding)))

(defn split-on-silence
  "splits audio seg on partial audio segs (on detected silence)"
  [seg & {:keys [silence-len silence-thresh padding]
          :or {silence-len 1000, silence-thresh -30, padding 100}}]
  {:pre (padding > silence-len)}
  (split_on_silence seg :min_silence_len silence-len
                    :silence_thresh silence-thresh :keep_silence padding))

(defn compress-dyn-range
  "threshold = -20.0
     Threshold in dBFS. default of -20.0 means -20dB relative to the
     maximum possible volume. 0dBFS is the maximum possible value so
     all values for this argument sould be negative.

  ratio = 4.0
     Compression ratio. Audio louder than the threshold will be
     reduced to 1/ratio the volume. A ratio of 4.0 is equivalent to
     a setting of 4:1 in a pro-audio compressor like the Waves C1.

  attack = 5.0
     Attack in milliseconds. How long it should take for the compressor
     to kick in once the audio has exceeded the threshold.

  release = 50.0
     Release in milliseconds. How long it should take for the compressor
     to stop compressing after the audio has falled below the threshold.

  For an overview of Dynamic Range Compression, see:
  http://en.wikipedia.org/wiki/Dynamic_range_compression"
  [seg & {:keys [thresh ratio attack release]
          :or {thresh -20, ratio -4, attack 5, release 50}}]
  (py.. seg (compress_dynamic_range :threshold thresh :ratio ratio
                                    :attack attack :release release)))

(defn low-pass
  "low pass filter

  cutoff : int
     frequency (in Hz) where higher frequency signal will begin to
     be reduced by 6dB per octave (doubling in frequency) above this point

  order = 5 : int
     nth order butterworth filter (default: 5th order). The
     attenuation is -6dB/octave beyond the cutoff frequency (for 1st
     order). A Higher order filter will have more attenuation, each level
     adding an additional -6dB (so a 3rd order butterworth filter would
     be -18dB/octave)."
  [seg cutoff & {:keys [order] :or {order 5}}]
  (py.. seg (low_pass_filter cutoff)))

(defn high-pass
  "high pass filter

  cutoff: int
     frequency (in Hz) where lower frequency signal will begin to
     be reduced by 6dB per octave (doubling in frequency) below this point

  order = 5 : int
     nth order butterworth filter (default: 5th order). The
     attenuation is -6dB/octave beyond the cutoff frequency (for 1st
     order). A Higher order filter will have more attenuation, each level
     adding an additional -6dB (so a 3rd order butterworth filter would
     be -18dB/octave)."
  [seg cutoff & {:keys [order] :or {order 5}}]
  (py.. seg (high_pass_filter cutoff :order order)))

(defn band-pass
  "band pass filter

  low-cutoff: int
     frequency (in Hz) where higher frequency signal will begin to
     be reduced by 6dB per octave (doubling in frequency) above this point

  high-cutoff: int
     frequency (in Hz) where lower frequency signal will begin to
     be reduced by 6dB per octave (doubling in frequency) below this point

  order = 5: int
     nth order butterworth filter (default: 5th order). The
     attenuation is -6dB/octave beyond the cutoff frequency (for 1st
     order). A Higher order filter will have more attenuation, each level
     adding an additional -6dB (so a 3rd order butterworth filter would
     be -18dB/octave)."
  [seg low-cutoff high-cutoff & {:keys [order] :or {order 5}}]
  (py.. seg (band_pass_filter low-cutoff high-cutoff :order order)))
