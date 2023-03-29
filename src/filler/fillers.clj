(ns filler.fillers
  (:require [babashka.fs :as fs]
            [filler.utils :as utils]))

(defn- file-to-filler [file]
  (let [config-data (utils/read-config-file (str file))
        path (fs/parent file)
        name (fs/file-name path)]
    {:name name
     :path (str path)
     :description (:description config-data) 
     :files (:files config-data)}))

(defn- list-fillers []
  (let [root-path (:path (utils/read-config-file utils/CONFIG-LOCATION))
        files (fs/glob root-path "**.edn")]
    (map file-to-filler files)))

(defn- print-filler-data [filler]
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

(defn list []
  (let [fillers (list-fillers)]
    (println "# We found" (count fillers) "fillers in the system")
    (doseq [filler fillers]
      (newline)
      (print-filler-data filler))))

(defn compare-filler-by-name [filler name]
  (println "comp" filler name)
  (when (= (:name filler) name) filler))

(defn- load-fillers-by-name [fillers name]
  (first (filter (comp #{name} :name) fillers)))


(defn update-file-with-env-vars [path]
  (let [content (slurp path)]
    (when (utils/exists-variables? content)
      (println "\n-> Updating the file" path "to use environment variables")
      (spit path (utils/replace-variables content (utils/load-vars-and-values content))))))

(defn run-filler
  {:org.babashka/cli {:coerce {:names [:string]}}}
  [{names :names}]
  (doseq [name names]
    (let [filler (load-fillers-by-name (list-fillers) name)]
      (newline)
      (print-filler-data filler)
      (newline)
      (doseq [file (:files filler)]
        (let [path (:path filler)
              src (str path fs/file-separator (:src file))
              dst (str (fs/expand-home (:dst file)))]
          (println "-> Copying file \nfrom:" src "\nto:" dst)
          (fs/copy src dst {:replace-existing true})
          (update-file-with-env-vars dst))))))
