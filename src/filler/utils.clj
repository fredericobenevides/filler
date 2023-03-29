(ns filler.utils
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]))

(def CONFIG-LOCATION (str (fs/expand-home "~/.filler.edn")))

(defn create-config
  "Creates the config file inside the CONFIG_LOCATION containing a key
   :path that points to a external path which allows the filler to
   load content from this path"
  [path]
  (fs/create-file CONFIG-LOCATION)
  (spit CONFIG-LOCATION {:path path}))

(defn read-config-file
  "Returns the content of file config configured using edn"
  [path]
  (edn/read-string (slurp path)))

(defn get-env-value
  "Returns an environment value based on the name"
  [name]
  (System/getenv name))
