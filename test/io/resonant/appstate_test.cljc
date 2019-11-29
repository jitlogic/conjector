(ns io.resonant.appstate-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.appstate :as cap]))


(defn test-init [{:keys [config old-state]}]
  {:foo (:foo config),
   :bar (:foo old-state)})


(defn test-shutdown [{:keys [old-state]}]
  (assoc old-state :shut :down))


(def SYSTEM
  {:a {:init test-init, :shutdown test-shutdown, :schema :bar}
   :b {:init test-init, :schema :baz, :requires [[:a]]}
   :c {:init test-init, :schema :bag :requires [[:b]], :before [[:d]]}
   :d {:init test-init, :shutdown test-shutdown}})


(def CONFIG-1
  {:a {:foo "foo-a"}
   :b {:foo "foo-b"}
   :c {:foo "foo-c"}
   :d {:foo "foo-d"}})


(def CONFIG-2
  {:a {:foo "foo-1"}
   :b {:foo "foo-2"}
   :c {:foo "foo-3"}
   :d {:foo "foo-4"}})


(deftest test-extract-schema
  (is (= {:a :bar, :b :baz, :c :bag, :d :foo}
         (cap/extract SYSTEM :schema :foo))))


(deftest test-init-shutdown
  (let [s1 (cap/init SYSTEM CONFIG-1 {})
        s2 (cap/init SYSTEM CONFIG-2 s1)
        s3 (cap/shutdown SYSTEM CONFIG-2 s2)]
    (is (= {:a {:foo "foo-a", :bar nil},
            :b {:foo "foo-b", :bar nil},
            :c {:foo "foo-c", :bar nil},
            :d {:foo "foo-d", :bar nil}}
           s1))
    (is (= {:a {:foo "foo-1", :bar "foo-a"},
            :b {:foo "foo-2", :bar "foo-b"},
            :c {:foo "foo-3", :bar "foo-c"},
            :d {:foo "foo-4", :bar "foo-d"}}
           s2))
    (is (= {:a {:foo "foo-1", :bar "foo-a", :shut :down},
            :b {:foo "foo-2", :bar "foo-b"},
            :c {:foo "foo-3", :bar "foo-c"},
            :d {:foo "foo-4", :bar "foo-d", :shut :down}}
           s3))))

