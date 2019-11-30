(ns io.resonant.process-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.process :as rcp]))


(defn test-proc-fn [counter log {:keys [path pdef data all-data]}]
  (swap! log conj path)
  (into {:c (swap! counter inc), :cd (:test pdef), :all (:test all-data)} data))


(def PROC-ARGS {:proc-node? :test, :requires :requires, :before :before})


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
  (let [x (rcp/deps->exact PROC-ARGS NODES-DP1)]
    (is (= [[:a]] (-> x second second :requires)))))


(deftest test-deps-pairs
  (is (= [[[:b] [:a :x]] [[:c] [:b]] [[:d] [:c]]]
         (rcp/deps->pairs PROC-ARGS NODES-DP1))))


(deftest test-deps-seq
  (is (= [[:a] [:b] [:c] [:d]]
         (rcp/deps->seq [[[:b] [:a]] [[:c] [:b]] [[:d] [:c]]]))))


(def CDEFS
  {:a {:test :foo}
   :b {:test :bar, :requires [[:a]]}
   :c {:test :baz, :requires [[:a] [:b]], :before [[:d]]}
   :d {:test :bag}
   :e {:1 {:test :e1, :requires [[:b]] :before [[:d]]}
       :2 {:test :e2, :before [[:d]]}}
   })


(def DATA-A
  {:a {:foo :bar}
   :b {:foo :baz}
   :c {:foo :bag}
   :e {:1 {:foo "e1"}
       :2 {:foo "e2"}}})


(def DATA-B
  {:a {:FOO "bar"}
   :b {:FOO "baz"}
   :c {:FOO "bag"}
   :e {:1 {:FOO "e1"}
       :2 {:FOO "e2"}}})


(deftest test-process
  (let [log (atom []),
        proc-args (assoc PROC-ARGS :proc-fn (partial test-proc-fn (atom 0) log)),
        state (rcp/process proc-args CDEFS {:test :FOO-BAR, :a DATA-A, :b DATA-B})]
    (is (map? (:a state)))
    (are [c p]
      (= c (get-in state p))
      1 [:e :2 :c]
      2 [:a :c]
      3 [:b :c]
      4 [:e :1 :c]
      5 [:c :c]
      6 [:d :c])))


