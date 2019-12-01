(ns io.resonant.utils-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.utils :as rcu]))

(deftest test-to-path
  (is (= [:a :b :c] (rcu/to-path 'a:b:c)))
  (is (= [:a :b :c] (rcu/to-path [:a :b :c])))
  (is (= [:a :b :c] (rcu/to-path "a:b:c")))
  (is (= [:a] (rcu/to-path :a))))
