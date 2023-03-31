(ns filler.fillers-test
  (:require [babashka.fs :as fs]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [filler.fillers :as fillers]
            [filler.templates :as templates]
            [filler.utils :as utils]
            [helpers]))

(def filler-system-config (atom {:path "file"})) 

(def fillers (atom []))

(def fixtures-config (atom {}))

(defn init-fixtures-config []
  (let [config-dir (str (helpers/temp-dir))
        config-file (str (fs/create-file (fs/file config-dir "filler.edn")))
        fillers-dir (str (helpers/temp-dir))]
    (swap! filler-system-config merge {:path fillers-dir})
    (swap! fixtures-config merge {:config-dir config-dir
                                  :config-file config-file
                                  :fillers-dir fillers-dir}))
  (spit (:config-file @fixtures-config) {:path (:fillers-dir @fixtures-config)}))

(defn init-fillers-config []
  (dotimes [n 2]
    (let [fillers-dir (:fillers-dir @fixtures-config)
          filler-dir (fs/file fillers-dir (str "name" n))
          from (str (fs/file "{{filler_dir}}" "from" "file.txt"))
          to (str (fs/file filler-dir "to" "file.txt"))]
      (swap! fillers conj {:name (str "name" n)
                           :description (str "description" n)
                           :files [{:from from
                                    :to to}]}))))
(defn spit-fillers-config []
  (doseq [filler @fillers]
    (let [files (:files filler)
          to (:to (first files))
          filler-dir (str (fs/parent (fs/parent to)))
          config-path (str (fs/file filler-dir "config.edn"))]
      (fs/create-dirs filler-dir)
      (spit config-path filler))))

(defn spit-fillers-from-file []
  (doseq [filler @fillers]
    (let [files (:files filler)
          to (:to (first files))
          filler-dir (str (fs/parent (fs/parent to)))
          file-dir-from (str (fs/file filler-dir "from"))
          file-path (str (fs/file file-dir-from "file.txt"))]
      (fs/create-dirs file-dir-from)
      (spit file-path "content"))))

(defn init-fixtures []
  (init-fixtures-config)
  (init-fillers-config)
  (spit-fillers-config)
  (spit-fillers-from-file))

(init-fixtures)

(deftest create-filler-test
  (let [path (str (fs/file "{{filler_dir}}" "filler_file"))
        config (assoc (first @fillers) :path path)
        filler (fillers/create-filler config)]
    (is (= "name0" (:name filler)))
    (is (= "description0" (:description filler)))
    (is (= "{{filler_dir}}" (:path filler)))
    (is (string/includes? (:from (first (:files filler))) "file.txt"))))

(deftest find-all-fillers-config
  (let [fillers-dir (:fillers-dir @fixtures-config)
        fillers (fillers/find-all-fillers-config fillers-dir)]
    (is (= (count fillers) 2))
    (is (every? #{"name0" "name1"} [(:name (first fillers)) (:name (second fillers))]))))

(deftest find-all-fillers-test
  (let [fillers-dir (:fillers-dir @fixtures-config)
        config-file (fs/file (helpers/temp-dir) "config.edn")
        _ (spit config-file {:path fillers-dir})]
    (with-redefs [utils/CONFIG-LOCATION config-file]
      (let [fillers (fillers/find-all-fillers)]
        (is (filter (complement empty?) (map :path fillers)))))))

(deftest find-fillers-by-name-test
  (let [config (assoc (first @fillers) :path "path")]
    (testing "find by valid name"
      (prn "config" config)
      (let [filler (fillers/create-filler config)]
        (is (fillers/find-fillers-by-name [filler] "name"))))
    (testing "find by invalid name"
      (let [filler (fillers/create-filler config)]
        (is (not (seq (fillers/find-fillers-by-name [filler] "invalid"))))))))

(deftest execute-filler-test
  (let [config-file (:config-file @fixtures-config)
        config1 (first @fillers)
        config2 (first @fillers)
        to1 (:to (first (:files config1)))
        to2 (:to (first (:files config2)))
        ]
    (with-redefs [utils/CONFIG-LOCATION config-file]
      (let [fillers (fillers/find-all-fillers)]
        (fillers/execute-filler (first fillers))
        (fillers/execute-filler (second fillers))
        (is (fs/exists? to1))
        (is (fs/exists? to2))))))
