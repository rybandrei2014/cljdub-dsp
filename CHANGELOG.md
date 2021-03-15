# Changelog

## 1.0.0
### Added
- interface for pydub functionality
- interface for TarsosDSP functionality
- interface for interoperation between pydub and TarsosDSP

## 1.1.0
### Added
- unit tests for util namespace
### Removed
- redundant !nil?->, doto-when from util namespace
### Changed
- better implementation for doto-for macro

## 1.1.1
### Removed
- redundant code from util_test

## 1.1.2
### Changed
- HOTFIX: implementation of doto-for macro

## 1.1.3
### Changed
- HOTFIX: implementation of doto-for macro
- HOTFIX: pre condition for ->ChunkedBufferWriter
- small refactor of example/main.clj

## 1.2.0
### Added
- unit tests for prop, io, operation, effect namespaces

## 1.3.0
### Changed
- use other fork of TarsosDSP library - com.github.dragoon000320/tarsosdsp
- fixed tests for util namespace - util_test.clj
- disable zeroPadLastBuffer on AudioDispatcher in apply-processors-gen
### Added
- tests for dsp.model and dsp.interop namespaces

## 1.4.0
### Added
- tests for dsp.result and dsp.processor namespaces

## 1.4.1
### Changed
- reference TarsosTranscoder fork from maven central instead of local .jar
- fixed .gitignore to ignore calva related files