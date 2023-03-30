(ns helpers
  (:require [babashka.fs :as fs]))

(defn temp-dir []
  (-> (fs/create-temp-dir)
      (fs/delete-on-exit)))

(defn create-file
  ([file] (create-file (temp-dir) file))
  ([path file] (fs/file path file)))
