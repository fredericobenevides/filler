(ns filler.utils-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing]]
            [clojure.edn :as edn]
            [filler.utils :as utils]
            [helpers]))

(deftest create-config-test
  (let [file (fs/file (helpers/temp-dir) "file.edn")]
    (with-redefs [utils/CONFIG-LOCATION file]
      (utils/create-config "external_project")
      (is (= "external_project"
             (:path (edn/read-string (slurp file))))))))

(deftest read-config-file-test
  (let [file (fs/file (helpers/temp-dir) "file.edn")
        _ (spit file {:key "value"})]
    (is (= "value" (:key (utils/read-config-file file))))))

(deftest get-env-value-test
  (testing "value exists"
    (is (not (= nil (utils/get-env-value "HOME")))))
  (testing "value doesn't exists"
    (is (= nil (utils/get-env-value "ANYOTHERVALUE")))))

(deftest create-path
  (testing "full path"
    (is (= "/opt/filler/file.txt" 
           (utils/create-path "/opt/filler/file.txt"))))
  (testing "expand home"
    (is (= (str (fs/expand-home "~/filler/file.txt"))
           (utils/create-path "~/filler/file.txt"))))
  (testing "create path with tags"
    (testing "with variables"
      (is (= (str (fs/expand-home "/opt/filler/file.txt"))
             (utils/create-path "{{filler_dir}}/filler/file.txt" "filler_dir" "/opt"))))
    (testing "without variables"
      (is (= (str (fs/expand-home "/opt/filler/file.txt"))
             (utils/create-path "/opt/filler/file.txt" "filler_dir" "/opt"))))))
