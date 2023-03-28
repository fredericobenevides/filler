(ns filler.utils
  (:require [babashka.fs :as fs]))

(def CONFIG-LOCATION (.toString (fs/expand-home (str "~/" ".filler.edn"))))

(defn create-config [path]
  (fs/create-file CONFIG-LOCATION)
  (spit CONFIG-LOCATION {:path path}))
