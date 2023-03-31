(ns filler.templates-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [filler.templates :as templates]))

(deftest exists-tags-test
  (is (seq (templates/exists-tags? "Contains valid tag {{FILLER_TAG}}")))
  (is (not (seq (templates/exists-tags? "Contains invalid tag {{OTHER_TAG}}"))))
  (is (not (seq (templates/exists-tags? "It doesn't contains tag")))))

(deftest read-tags-test
  (is (= (seq ["FILLER_TAG_1" "FILLER_TAG_2"])
         (templates/read-tags "Many tags {{FILLER_TAG_1}} {{FILLER_TAG_2}}"))))

(deftest replace-tag-with-value-test
  (testing "Containing valid tag"
    (is (= "My updated tag Hello World"
           (templates/replace-tag-with-value "My updated tag {{FILLER_TAG}}"
                                             "FILLER_TAG"
                                             "Hello World"))))
  (testing "Containing invalid tag"
    (is (= "My updated tag {{FILLER_TAG}}"
           (templates/replace-tag-with-value "My updated tag {{FILLER_TAG}}"
                                             "OTHER_TAG"
                                             "Hello World")))))

(deftest create-tag-data-test
  (is (= [{:tag "FILLER_TAG_1" :value "TAG_1"}
          {:tag "FILLER_TAG_2" :value "TAG_2"}]
         (templates/create-tag-data #(string/replace % #"FILLER_" "") ["FILLER_TAG_1" "FILLER_TAG_2"]))))

(deftest replace-multiple-tags-test
  (testing "Containing all the tags"
    (is (= "Hello World and Welcome"
           (templates/replace-multiple-tags "{{FILLER_TAG_1}} {{FILLER_TAG_2}} and {{FILLER_TAG_3}}"
                                            [{:tag "FILLER_TAG_1" :value "Hello"}
                                             {:tag "FILLER_TAG_2" :value "World"}
                                             {:tag "FILLER_TAG_3" :value "Welcome" }]))))
  (testing "Missing tags"
    (is (= "Hello World and {{FILLER_TAG_3}}"
           (templates/replace-multiple-tags "{{FILLER_TAG_1}} {{FILLER_TAG_2}} and {{FILLER_TAG_3}}"
                                            [{:tag "FILLER_TAG_1" :value "Hello"}
                                             {:tag "FILLER_TAG_2" :value "World"}]))))
  (testing "Empty value"
    (is (= "Hello {{FILLER_TAG_2}} and {{FILLER_TAG_3}}"
           (templates/replace-multiple-tags "{{FILLER_TAG_1}} {{FILLER_TAG_2}} and {{FILLER_TAG_3}}"
                                            [{:tag "FILLER_TAG_1" :value "Hello"}
                                             {:tag "FILLER_TAG_2" :value ""}])))))
