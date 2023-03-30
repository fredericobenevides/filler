(ns filler.utils-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is]]
            [clojure.edn :as edn]
            [filler.utils :as utils]
            [helpers]))

(deftest create-config-test
  (let [file (helpers/create-file "file.edn")]
    (with-redefs [utils/CONFIG-LOCATION file]
      (utils/create-config "external_project")
      (is (= "external_project"
             (:path (edn/read-string (slurp file))))))))

(deftest read-config-file-test
  (let [file (helpers/create-file "file.edn")
        _ (spit file {:key "value"})]
    (is (= "value" (:key (utils/read-config-file file))))))

(deftest find-all-config-files-test
  (let [root (helpers/temp-dir)
        sub-dir1 (fs/create-dir (fs/file root "sub1"))
        file1 (helpers/create-file sub-dir1 "file1.edn")
        _ (spit file1 {:path "content1"})
        sub-dir2 (fs/create-dir (fs/file root "sub2"))
        file2 (helpers/create-file sub-dir2 "file2.edn")
        _ (spit file2 {:path "content2"})
        all-files (utils/find-all-config-files (str root))]
    (is (= (count all-files) 2))
    (is (= "content1" (:path (first all-files))))
    (is (= "content2" (:path (second all-files))))))

(deftest get-env-value-test
  (is (not (= nil (utils/get-env-value "HOME")))))
