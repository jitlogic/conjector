(defproject io.resonant/conjector "0.0.4"
  :description "Application state management with dependency injection (kind of)"
  :url "http://github.com/jitlogic/conjector"
  :license {:name "The MIT License", :url "http://opensource.org/licenses/MIT"}
  :dependencies
  [[org.clojure/clojure "1.10.1" :scope "provided"]
   [com.stuartsierra/dependency "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "1.10.773"]]}})
