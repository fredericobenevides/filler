(ns filler.fillers
  (:require [babashka.fs :as fs]
            [filler.templates :as templates]
            [filler.utils :as utils]))

(defn create-filler
  "Returns a map containing the structure of the filler"
  [{:keys [name path description files]}]
  {:name name
   :path path
   :description description
   :files files})

(defn print-filler
  "Printns the filler data"
  [filler]
  (println "#" (:description filler))
  (println "name:" (:name filler))
  (println "path:" (:path filler))
  (println "file data")
  (doseq [file (:files filler)]
    (let [path (:path filler)
          src (str path fs/file-separator (:src file))
          dst (str (fs/expand-home (:dst file)))]
      (println "  source:" src)
      (println "  destination:" dst))))

(defn find-all-fillers-config
  "Returns all edn config files found in the path recursively"
  [path]
  (map (fn [f] (assoc (utils/read-config-file (str f)) :path (str f)))
       (fs/glob path "**/*.edn")))

(defn find-all-fillers
  "Returns all the edn files located in the utils/CONFIG-LOCATION
   converted as a filler"
  []
  (let [root-path (:path (utils/read-config-file utils/CONFIG-LOCATION))]
    (map create-filler (find-all-fillers-config root-path))))

(defn find-fillers-by-name
  "Returns a filler by its name"
  [fillers name]
  (filter #(= name (:name %)) fillers))

(defn execute-filler
  "Execute a filler"
  [filler]
  (let [filler-path (str (fs/expand-home (:path filler)))
        files (:files filler)]
    (doseq [file files]
      (let [src-path (str (fs/expand-home (:src file)))
            dst-path (:dst file)
            parent-dst-path (str (fs/parent dst-path))]
        (when (not (fs/exists? src-path))
          (println "File not found to be copied:" src-path))
        (println "Copying files from" src-path "to" dst-path)
        (fs/create-dirs (fs/file parent-dst-path))
        (fs/copy src-path dst-path [:replace-existing :copy-attributes])))))

(defn update-file-with-env-vars [path]
  (let [content (slurp path)]
    (when (templates/exists-tags? content)
      (println "\n-> Updating the file" path "to use environment variables")
      (spit path (templates/replace-multiple-tags content (templates/create-tag-data utils/get-env-value (templates/read-tags content)))))))
