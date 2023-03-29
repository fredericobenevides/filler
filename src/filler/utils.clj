(ns filler.utils
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]))

(def CONFIG-LOCATION (.toString (fs/expand-home (str "~/" ".filler.edn"))))

(defn create-config [path]
  (fs/create-file CONFIG-LOCATION)
  (spit CONFIG-LOCATION {:path path}))

(defn read-config-file [path]
  (edn/read-string (slurp path)))

(defn load-env-value [variable]
  (System/getenv variable))
