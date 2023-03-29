(ns filler.init-test
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [filler.init :as init]
            [filler.utils :as utils]))

(defn temp-dir []
  (-> (fs/create-temp-dir)
      (fs/delete-on-exit)))

(deftest init-test
  (testing "file already exist in the system"
    (let [tmp-dir (temp-dir)
          file (fs/file tmp-dir "file.edn")
          _ (spit file {:key "value"})]
      (with-redefs [utils/CONFIG-LOCATION file]
        (let [output (with-out-str (init/init))]
          (is (string/includes? output "already exists"))))))
  (testing "file doesn't exist in the system"
    (let [tmp-dir (temp-dir)
          file (fs/file tmp-dir "file.edn")]
      (with-redefs [utils/CONFIG-LOCATION file]
        (with-in-str "external-path" (init/init))
        (is (fs/exists? (str file)))
        (is (= "external-path"
               (-> (str file)
                   (slurp)
                   (edn/read-string)
                   (:path))))))))

(deftest clear-test
  (testing "file doesn't exist in the system"
    (let [tmp-dir (temp-dir)
          file (fs/file tmp-dir "file.edn")]
      (with-redefs [utils/CONFIG-LOCATION file]
        (let [output (with-out-str (init/clear))]
          (is (string/includes? output "does not exists"))))))
  (testing "file exist in the system"
    (let [tmp-dir (temp-dir)
          file (fs/file tmp-dir "file.edn")
          _ (spit file {:key "value"})]
      (with-redefs [utils/CONFIG-LOCATION file]
        (let [output (with-out-str (init/clear))]
          (is (not (fs/exists? file)))
          (is (string/includes? output "Removing filler")))))))
