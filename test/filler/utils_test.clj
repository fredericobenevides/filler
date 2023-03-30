(ns filler.utils-test
  (:require [clojure.test :refer [deftest is]]
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

(deftest get-env-value-test
  (is (not (= nil (utils/get-env-value "HOME")))))
