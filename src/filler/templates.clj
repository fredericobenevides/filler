(ns filler.templates
  (:require [clojure.string :as string]))

(def FILLER-TAG #"[{]+(FILLER_\w+)[}]+")

(defn exists-tags?
  "Returns true if exists tags inside the string"
  [s]
  (re-find FILLER-TAG s))

(defn read-tags
  "Returns the tags found in the string"
  [s]
  (let [vars (re-seq FILLER-TAG s)]
    (map (comp conj second) vars)))

(defn replace-tag-with-value
  "Replaces a string containing a tag with specific value"
  [s tag value]
  (when (and tag value)
    (let [pattern (re-pattern (str "[{]+" tag "[}]+"))]
      (string/replace s pattern value))))

(defn create-tag-data
  "Returns the tag data with the format of {:keys [:tag :value]]} consisting
  of the result of applying f to create a value based on the sequence of the
  tag's name"
  [f tags]
  (map (fn [tag] {:tag tag :value (f tag)}) tags))

(defn replace-multiple-tags
  "Allows to replace a string containing multiple tags. The tags should contain
   the tag name and its value. The tags should be a tag data with the
   following format: {:keys [:tags :value ]}"
  [s tags]
  (loop [s s tags tags]
    (if (empty? tags)
      s
      (let [tag (:tag (first tags))
            value (:value (first tags))]
        (if (not (string/blank? value))
          (recur (replace-tag-with-value s tag value) (next tags))
          (recur s (next tags)))))))
