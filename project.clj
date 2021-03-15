(defproject dragoon/cljdub-dsp "1.4.1"
  :description "Sound processing library that allows usage of pydub library's functionality in Clojure as well as provides interface for interaction between pydub and TarsosDSP libraries"
  :url "https://github.com/dragoon000320/cljdub-dsp"
  :license {:name "Eclipse Public License 2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-python/libpython-clj "1.46"]
                 [techascent/tech.datatype "5.0"]
                 [com.github.dragoon000320/tarsosdsp "1.0"]
                 [com.github.dragoon000320/tarsostranscoder "1.0"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]
                                  [com.clojure-goes-fast/clj-memory-meter "0.1.2"]
                                  [spieden/spyscope "0.1.7"]]
                   :injections [(use 'spyscope.core)
                                (use 'clj-memory-meter.core)]}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[cljdub-dsp \"[0-9.]*\"\\\\]/[cljdub-dsp \"${:version}\"]/" "README.md"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :bootclasspath true)
