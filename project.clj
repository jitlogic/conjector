(defproject io.resonant/conjector "0.0.3-SNAPSHOT"
  :description "Dependency tree component processing library"
  :url "http://resonant.io/projects/conjector"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [com.stuartsierra/dependency "0.2.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "1.10.597"]]}})
