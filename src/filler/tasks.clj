(ns filler.tasks
  (:require [babashka.fs :as fs]
            [filler.fillers :as fillers]
            [filler.utils :as utils]))

(defn init
  "Initialize the filler in the system creating a file called file
   $HOME/filler.edn"
  []
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

(defn clear
  "Removed the filler from the system"
  []
  (if (fs/exists? utils/CONFIG-LOCATION)
    (do
      (println "# Removing filler from the system")
      (fs/delete utils/CONFIG-LOCATION))
    (println "# Config file does not exists")))

(defn list
  "List all the fillers in the system"
  []
  (let [fillers (fillers/find-all-fillers)]
    (println "# We found" (count fillers) "fillers in the system")
    (doseq [filler fillers]
      (newline)
      (fillers/print-filler filler))))

(defn run-filler
  "Run the fillers based on their names"
  {:org.babashka/cli {:coerce {:names [:string]}}}
  [{names :names}]
  (doseq [name names]
    (doseq [filler (fillers/find-fillers-by-name (fillers/find-all-fillers) name)]
      (newline)
      (fillers/print-filler filler)
      (newline)
      (fillers/execute-filler filler)
      )))
