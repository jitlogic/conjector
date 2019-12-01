(ns io.resonant.process-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.process :as rcp]))


(defn test-proc-fn [counter log {:keys [path pdef data all-data]}]
  (swap! log conj path)
  (into {:c (swap! counter inc), :cd (:test pdef), :all (:test all-data)} data))


(def NODES-DP1
  [[[:a] {}]
   [[:b] {:requires [[:a :x]]}]
   [[:c] {:requires [[:b]], :before [[:d]]}]
   [[:d] {}]])


(deftest test-deps-best
  (is (= [:b] (rcp/deps-best [[:a] [:b] [:c]] [:b])))
  (is (= [:b] (rcp/deps-best [[:a] [:b] [:c]] [:b :a])))
  (is (= [:b :a] (rcp/deps-best [[:a] [:b] [:b :a] [:c]] [:b :a :c]))))


(deftest test-deps-exact
  (let [x (rcp/deps->exact NODES-DP1)]
    (is (= [[:a]] (-> x second second :requires)))))


(deftest test-deps-pairs
  (is (= [[[:b] [:a :x]] [[:c] [:b]] [[:d] [:c]]]
         (rcp/exact->pairs NODES-DP1))))


(deftest test-deps-seq
  (is (= [[:a] [:b] [:c] [:d]]
         (rcp/pairs->seq [[[:b] [:a]] [[:c] [:b]] [[:d] [:c]]]))))


(def SYSTEM1
  {:a {:test :foo}
   :b {:test :bar, :requires [[:a]]}
   :c {:test :baz, :requires [[:a] [:b]], :before [[:d]]}
   :d {:test :bag}
   :e {:1 {:test :e1, :requires [[:b]] :before [[:d]]}
       :2 {:test :e2, :before [[:d]]}}
   })


(def DATA1
  {:a {:foo :bar}
   :b {:foo :baz}
   :c {:foo :bag}
   :e {:1 {:foo "e1"}
       :2 {:foo "e2"}}})


(def DATA2
  {:a {:FOO "bar"}
   :b {:FOO "baz"}
   :c {:FOO "bag"}
   :e {:1 {:FOO "e1"}
       :2 {:FOO "e2"}}})


(def SYSTEM2
  {:a {:test :foo, :proc-order 2}
   :b {:test :bar, :proc-order 1}
   :c {:test :baz, :proc-order 3}
   :d {:test :bag, :requires [[:e]]}
   :e {:test :baf}})


(deftest test-process-order
  (is (= [[:e :2] [:a] [:b] [:e :1] [:c] [:d]]
         (rcp/process-order {:proc-node? :test} SYSTEM1)))
  (is (= [[:b] [:a] [:c] [:e] [:d]]
         (rcp/process-order {:proc-node? :test} SYSTEM2))))


(deftest test-process
  (let [log (atom []),
        proc-args {:proc-node? :test :proc-fn (partial test-proc-fn (atom 0) log)},
        state (rcp/process proc-args SYSTEM1 {:test :FOO-BAR, :a DATA1, :b DATA2})]
    (is (map? (:a state)))
    (are [c p]
      (= c (get-in state p))
      1 [:e :2 :c]
      2 [:a :c]
      3 [:b :c]
      4 [:e :1 :c]
      5 [:c :c]
      6 [:d :c])))

