(ns io.resonant.component-test
  (:require
    [clojure.test :refer :all]
    [io.resonant.conjector.component :as rcm]))

(deftest test-parse-args-map
  (is (= '{:args {:keys [foo bar] :as x}, :requires ([:app :foo] [:app :bar])}
         (#'rcm/parse-args-map [:app] '{:keys [foo bar] :as x})))
  (is (= '{:args {{:keys [foo bar]} :baz}, :requires ([:app :baz :foo] [:app :baz :bar])}
         (#'rcm/parse-args-map [:app] '{{:keys [foo bar]} :baz})))
  (is (= '{:args {[a b c] :x}, :requires [[:app :x]]}
         (#'rcm/parse-args-map [:app] '{[a b c] :x})))
  (is (= '{:args {x :foo, y :bar}, :requires ([:foo] [:bar])}
         (#'rcm/parse-args-map [] '{x :foo y :bar}))))

(deftest test-parse-component-bindings
  (is (= '{:args {{:keys [foo bar]} :app-state}, :requires ([:foo] [:bar])}
         (#'rcm/parse-component-bindings '[{{:keys [foo bar]} :app-state}])))
  (is (= '{:args _} (#'rcm/parse-component-bindings []))))


(deftest test-parse-component-args
  (is (= {:init '(println "ojaaa")}
         (#'rcm/parse-component-args '[:init (println "ojaaa")])))
  (is (= {:init '(println "ojaaa")}
         (#'rcm/parse-component-args '[(println "ojaaa")])))
  (is (= {:doc "DOC",
          :before [[:foo]], :requires [[:bar]],
          :config-schema ::config, :state-schema ::state,
          :shutdown {:shut :down},
          :init '(do (println "ojaaa") {:init :comp})}
         (#'rcm/parse-component-args
           '["DOC"
             :before [[:foo]], :requires [[:bar]],
             :config-schema ::config, :state-schema ::state,
             :shutdown {:shut :down},
             (println "ojaaa")
             {:init :comp}])))
  (is (= {:init :foo, :shutdown :bar}
         (#'rcm/parse-component-args [:init :foo, :shutdown :bar]))))


(deftest test-parse-component-macro
  (let [c (rcm/component [{{:keys [foo bar], {:keys [cnt]} :stats} :app-state}]
            "Some doc"
            :before [:baz:bak], :requires [:bag]
            :config-schema {:foo :any, :bar :any}
            :state-schema {:foo :str, :bar :str, :stats {:cnt :atom}}
            (swap! cnt inc)
            {:foo (str foo), :bar (str bar)})
        cnt (atom 0)]
    (is (= [[:baz :bak]] (:before c)))
    (is (= [[:foo] [:bar] [:bag] [:stats :cnt]]) (:requires c))
    (is (= "Some doc" (:doc c)))
    (is (= {:foo :any, :bar :any}) (:config-schema c))
    (is (= {:foo :str, :bar :str, :stats {:cnt :atom}} (:state-schema c)))
    (is (fn? (:init c)))
    (is (= {:foo "aj", :bar "waj"} ((:init c) {:app-state {:foo 'aj, :bar 'waj, :stats {:cnt cnt}}})))
    (is (= 1 @cnt))))

(rcm/defcomponent foo:bar [{{:keys [foo bar], {:keys [cnt]} :stats} :app-state}]
  "Some doc"
  :before [:baz:bak], :requires [:bag]
  :config-schema {:foo :any, :bar :any}
  :state-schema {:foo :str, :bar :str, :stats {:cnt :atom}}
  (swap! cnt inc)
  {:foo (str foo), :bar (str bar)})

(deftest test-defcomponent
  (let [c (get-in @rcm/APP-DEF [:foo :bar])]
    (is (map? c))
    (is (= [[:baz :bak]] (:before c)))
    (is (= [[:foo] [:bar] [:bag] [:stats :cnt]]) (:requires c))
    (is (= "Some doc" (:doc c)))
    (is (= {:foo :any, :bar :any}) (:config-schema c))
    (is (= {:foo :str, :bar :str, :stats {:cnt :atom}} (:state-schema c)))
    (is (fn? (:init c))))
  (is (map? foo:bar)))