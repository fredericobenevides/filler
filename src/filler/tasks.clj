(ns filler.tasks
  (:require [babashka.fs :as fs]
            [filler.fillers :as fillers]
            [filler.utils :as utils]))

(defn init []
  (if (not (fs/exists? utils/CONFIG-LOCATION))
    (do
      (println "# Initializing the filler in the current system")
      
      (print "## Before creating the file. We need to load the folder containing all the files we're going to filler in the system. Please provide the path:")
      (print "\n## PATH: ")
      (flush)
      
      (let [path (read-line)]
        (utils/create-config (.toString (fs/expand-home path)))
        (println "# Config file created in this path:" utils/CONFIG-LOCATION))
      )
    (println "# Filler config already exists.")))

(defn clear []
  (if (fs/exists? utils/CONFIG-LOCATION)
    (do
      (println "# Removing filler from the system")
      (fs/delete utils/CONFIG-LOCATION))
    (println "# Config file does not exists")))

(defn list []
  (let [fillers (fillers/list-fillers)]
    (println "# We found" (count fillers) "fillers in the system")
    (doseq [filler fillers]
      (newline)
      (fillers/print-filler-data filler))))

(defn run-filler
  {:org.babashka/cli {:coerce {:names [:string]}}}
  [{names :names}]
  (doseq [name names]
    (let [filler (fillers/load-fillers-by-name (fillers/list-fillers) name)]
      (newline)
      (fillers/print-filler-data filler)
      (newline)
      (doseq [file (:files filler)]
        (let [path (:path filler)
              src (str path fs/file-separator (:src file))
              dst (str (fs/expand-home (:dst file)))]
          (println "-> Copying file \nfrom:" src "\nto:" dst)
          (fs/copy src dst {:replace-existing true})
          ;; (update-file-with-env-vars dst)
          )))))
