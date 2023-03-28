(ns filler.init
  (:require [babashka.fs :as fs]
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
