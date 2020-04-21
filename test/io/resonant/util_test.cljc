(ns io.resonant.util-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.util :as rcu]))

(deftest test-to-path
  (is (= [:a :b :c] (rcu/to-path 'a:b:c)))
  (is (= [:a :b :c] (rcu/to-path [:a :b :c])))
  (is (= [:a :b :c] (rcu/to-path "a:b:c")))
  (is (= [:a] (rcu/to-path :a)))
  (is (= [:a :b :c] (rcu/to-path :a:b:c))))
