(ns filler.utils
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as string]))

(def CONFIG-LOCATION (.toString (fs/expand-home (str "~/" ".filler.edn"))))

(def FILLER-ENV-VARIABLE #"[{]+(FILLER_\w+)[}]+")

(defn create-config [path]
  (fs/create-file CONFIG-LOCATION)
  (spit CONFIG-LOCATION {:path path}))

(defn read-config-file [path]
  (edn/read-string (slurp path)))

(defn exists-variables? [content]
  (re-find FILLER-ENV-VARIABLE content))

(defn load-variables [content]
  (let [vars (re-seq FILLER-ENV-VARIABLE content)]
    (map (comp conj second) vars)))

(defn replace-variable [content variable value]
  (when (and variable value)
    (let [pattern (re-pattern (str "[{]+" variable "[}]+"))]
      (string/replace content pattern value))))

(defn replace-variables [content tags]
  (loop [content content tags tags]
    (if (empty? tags)
      content
      (recur (replace-variable content (:tag (first tags)) (:value (first tags))) (next tags)))))

(defn load-env-value [variable]
  (System/getenv variable))

(defn load-vars-and-values [content]
  (map (fn [tag] {:tag tag :value (load-env-value tag)}) (load-variables content)))
