(ns filler.fillers-test
  (:require [babashka.fs :as fs]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [filler.fillers :as fillers]
            [filler.utils :as utils]
            [helpers]))

(def filler-system-config (atom {:path "file"})) 

(def filler-config-data (atom {:name "name"
                               :description "description"
                               :files [{:src "src.txt"
                                        :dst "dst.txt"}]}))

(def fixtures-config (atom {}))

(defn init-fixtures-config []
  (let [config-dir (str (helpers/temp-dir))
        config-file (str (fs/create-file (fs/file config-dir "filler.edn")))
        fillers-dir (str (helpers/temp-dir))]
    (swap! filler-system-config merge {:path fillers-dir})
    (swap! fixtures-config merge {:config-dir config-dir
                                  :config-file config-file
                                  :fillers-dir fillers-dir})))

(defn init-fixtures-fillers []
  (spit (:config-file @fixtures-config) {:path (:fillers-dir @fixtures-config)})
  (dotimes [n 2]
    (let [fillers-dir (:fillers-dir @fixtures-config)
          filler-subdir (str (fs/create-dir (fs/file fillers-dir (str "sub" n))))
          filler-config (str (fs/create-file (fs/file filler-subdir "config.edn")))]
      (swap! filler-config-data update-in [:files 0] merge {:src (str (fs/file filler-subdir (str "src" n ".txt")))})
      (swap! filler-config-data update-in [:files 0] merge {:dst (str (fs/file filler-subdir (str "dst" n ".txt")))})
      (fs/create-file (:src (first (:files @filler-config-data))))
      (spit filler-config (assoc @filler-config-data :name (str "name" n)))
      )))

(init-fixtures-config)
(init-fixtures-fillers)

(deftest create-filler-test
  (let [config (assoc @filler-config-data :path "file_location")
        filler (fillers/create-filler config)]
    (is (= "name" (:name filler)))
    (is (= "description" (:description filler)))
    (is (= "file_location" (:path filler)))
    (is (string/includes? (:src (first (:files filler))) "src1.txt"))))

(deftest find-all-fillers-config
  (let [fillers-dir (:fillers-dir @fixtures-config)
        fillers (fillers/find-all-fillers-config fillers-dir)]
    (is (= (count fillers) 2))
    (is (every? #{"name0" "name1"} [(:name (first fillers)) (:name (second fillers))]) )))

(deftest find-all-fillers-test
  (let [fillers-dir (:fillers-dir @fixtures-config)
        config-file (fs/file (helpers/temp-dir) "config.edn")
        _ (spit config-file {:path fillers-dir})]
    (with-redefs [utils/CONFIG-LOCATION config-file]
      (let [fillers (fillers/find-all-fillers)]
        (is (filter (complement empty?) (map :path fillers)))))))

(deftest find-fillers-by-name-test
  (testing "find by valid name"
    (let [filler (fillers/create-filler @filler-config-data)]
      (is (fillers/find-fillers-by-name [filler] "name"))))
  (testing "find by invalid name"
    (let [filler (fillers/create-filler @filler-config-data)]
      (is (not (seq (fillers/find-fillers-by-name [filler] "invalid")))))))

(deftest execute-filler-test
  (let [fillers-dir (:fillers-dir @fixtures-config)
        config-file (:config-file @fixtures-config)
        filler-subdir0 (str (fs/file fillers-dir "sub0"))
        filler-subdir1 (str (fs/file fillers-dir "sub1"))]
    (with-redefs [utils/CONFIG-LOCATION config-file]
      (let [fillers (fillers/find-all-fillers)]
        (fillers/execute-filler (first fillers))
        (fillers/execute-filler (second fillers))
        (is (fs/exists? (fs/file filler-subdir0 "dst0.txt")))
        (is (fs/exists? (fs/file filler-subdir1 "dst1.txt")))))))
