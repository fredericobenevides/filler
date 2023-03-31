(ns filler.fillers
  (:require [babashka.fs :as fs]
            [filler.templates :as templates]
            [filler.utils :as utils]))

(defn create-filler
  "Returns a map containing the structure of the filler"
  [{:keys [name path description files]}]
  (let [path (str (fs/parent path))
        filler {:name name
                :path path
                :description description}
        files (map (fn [f]
                     {:src (utils/create-path (:src f) "filler_dir" path)
                      :dst (utils/create-path (:dst f)) "filler_dir" path})
                   files)]
    (merge filler {:files files})))

(defn print-filler
  "Printns the filler data"
  [filler]
  (println "#" (:description filler))
  (println "name:" (:name filler))
  (println "path:" (:path filler))
  (println "file info:")
  (doseq [file (:files filler)]
    (let [src (:src file)
          dst (:dst file)]
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

(defn- update-file-with-env-vars
  "Update a file that is using a template tag to use the values from the
  environment"
  [path]
  (let [content (slurp path)]
    (when (templates/exists-tags? content)
      (println "\n-> Updating the file to use environment variables")
      (println "  file:" path)
      (spit path (templates/replace-multiple-tags content (templates/create-tag-data utils/get-env-value (templates/read-tags content)))))))

(defn execute-filler
  "Execute a filler"
  [filler]
  (let [files (:files filler)]
    (doseq [file files]
      (let [src-path (:src file)
            dst-path (:dst file)
            dst-parent-path (str (fs/parent dst-path))]
        (if (fs/exists? src-path)
          (do
            (println "-> Copying files")
            (println "  from:" src-path)
            (println "  to:" dst-path)
            (fs/create-dirs (fs/file dst-parent-path))
            (fs/copy src-path dst-path {:replace-existing true :copy-attributes true})
            (update-file-with-env-vars dst-path))
          (println "File not found to be copied:" src-path))))))


