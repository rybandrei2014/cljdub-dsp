;; require namespaces to work with pydub
(require '[cljdub-dsp.io :refer :all])
(require '[cljdub-dsp.prop :refer :all])
(require '[cljdub-dsp.operation :refer :all])
(require '[cljdub-dsp.effect :refer :all])
(require '[cljdub-dsp.util :refer :all])
;; require namespaces for interop between pydub and TarsosDSP libs
(require '[cljdub-dsp.dsp.interop :refer :all])
(require '[cljdub-dsp.dsp.model :refer :all])
(require '[cljdub-dsp.dsp.processor :refer :all])
(require '[cljdub-dsp.dsp.pitch :refer :all])

(comment
  ;; convert stereo mp3 file to stereo wav file
  ;; as cljdub-dsp requires wav input to process it
  (->wav "test.mp3" "test.wav" :stereo true)

  ;; creates AudioSegment instance from wav input
  ;; which will be further processed
  (-> (load-seg "test.wav")
      ;; applies stereo gain to the input
      (stereo-gain 0.5 2)
      ;; applies fade out to the input
      (fade-out 1)
      ;; reverses the input
      reversed
      ;; repeats the input 2 times
      (repeated 2)
      ;; writes it as a wav
      (write-seg "test-1-out.wav"))

  (-> (load-seg "test.wav")
      ;; takes only first 5 seconds of input audion
      (slice :end 5000)
      ;; plays the input
      play)

  (-> (load-seg "test.wav")
      (slice :end 60000)
      ;; applies Tarsos DSP processors chain on an input stereo AudioSegment
      (apply-processors-multi-channel
       {}
       ;; transposes sample rate by factor 1.5
       (transpose-rate 1.5)
       ;; applies gain
       (apply-gain 2))
      (write-seg "test-3-out.wav"))

  ;; convert stereo mp3 to mono wav
  (->wav "test.mp3" "test-mono.wav" :stereo false)

  (-> (load-seg "test-mono.wav")
      (slice :end 60000)
      (apply-processors
       ;; use custom buffer size and overlap
       {:buffer-size 1024 :buffer-overlap 512}
       ;; applies low frequency oscilation
       (amplitude-lfo 200 1.5)
       ;; applies an echo effect
       (delay-effect 2000 1 0.5))
      (write-seg "test-4-out.wav"))

  (-> (load-seg "test.wav")
      ;; slices audio segment from 2:01 to the end of audio
      (slice :end -121000)
      ;; strips silence from audio
      (strip-silence :silence-len 700 :silence-thresh -15)
      ;; splits tarsosdsp processment into chunks to speedup
      (chunkify
       #(apply-processors-multi-channel %
                                        {}
                                        (wsola-processor 0.75)
                                        (transpose-rate 0.75)))
      ;; reverses the input
      reversed
      (write-seg "test-5-out.wav"))
)
