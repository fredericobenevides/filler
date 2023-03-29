#!/usr/bin/env bb

(require '[clojure.test :as t]
         '[babashka.classpath :as cp])

(cp/add-classpath "src:test")                        

(require 'filler.templates-test)                  

(def test-results
  (t/run-tests 'filler.templates-test))           

(let [{:keys [fail error]} test-results]
  (when (pos? (+ fail error))
    (System/exit 1)))        
