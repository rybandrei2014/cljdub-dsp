# cljdub-dsp
[![Clojars Project](https://img.shields.io/clojars/v/dragoon/cljdub-dsp.svg)](https://clojars.org/dragoon/cljdub-dsp)

A Clojure sound processing library that utilizes <a href="https://github.com/cnuernber/libpython-clj">JNA libpython bindings library</a> to enable usage of <a href="https://github.com/jiaaro/pydub">pydub library's functionality</a> in Clojure. Also provides easy interface for interaction between <a href="https://github.com/JorenSix/TarsosDSP">TarsosDSP library</a> and pydub libraries.

## Get started

* install python 3.7, pip

* install python dependencies
```bash
pip3 install -r requirements.txt
```

* add dependency to the project.clj

## Usage

Code snippets about library usage are provided in **example** folder

## Contributions

It is an open-source project so contributions are welcomed.

## Special Thanks To

* <a href="https://github.com/cnuernber/libpython-clj">libpython-clj</a>
* <a href="https://github.com/jiaaro/pydub">pydub</a>
* <a href="https://github.com/JorenSix/TarsosTranscoder">TarsosTranscoder</a>
* <a href="https://github.com/JorenSix/TarsosDSP">TarsosDSP</a>
* <a href="https://github.com/techascent/tech.datatype">tech.datatype</a>

## License

Copyright © 2020 Andrei Rybin

Distributed under the <a href="https://www.eclipse.org/legal/epl-2.0/">Eclipse Public License 2.0</a>
