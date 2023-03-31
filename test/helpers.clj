(ns helpers
  (:require [babashka.fs :as fs]))

(defn temp-dir []
  (-> (fs/create-temp-dir)
      (fs/delete-on-exit)))
