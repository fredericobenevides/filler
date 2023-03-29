(ns filler.utils-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is]]
            [clojure.edn :as edn]
            [filler.utils :as utils]))

(defn temp-dir []
  (-> (fs/create-temp-dir)
      (fs/delete-on-exit)))

(deftest create-config-test
  (let [tmp-dir (temp-dir)
        file (fs/file tmp-dir "file.edn")]
    (with-redefs [utils/CONFIG-LOCATION file]
      (utils/create-config "external_project")
      (is (= "external_project"
             (:path (edn/read-string (slurp file))))))))

(deftest read-config-file-test
  (let [tmp-dir (temp-dir)
        file (fs/file tmp-dir "file.end")
        _ (spit file {:key "value"})]
    (is (= "value" (:key (utils/read-config-file file))))))

(deftest get-env-value-test
  (is (not (= nil (utils/get-env-value "HOME")))))
