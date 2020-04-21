(ns io.resonant.conjector.util
  (:require
    [clojure.string :as cs]))

(defn to-path [v]
  (cond
    (vector? v) (vec (apply concat (map to-path v)))
    (symbol? v) (vec (map keyword (cs/split (name v) #":")))
    (string? v) (vec (map keyword (cs/split v #":")))
    (keyword? v) (vec (for [s (cs/split (name v) #":")] (keyword s)))
    :else (throw (ex-info (str "Cannot convert to path: " v) {:arg v}))))

(defn null-wrapper [_ f] (f))

(defmacro with-wrapper [wrapper args & body]
  `((or ~wrapper null-wrapper) ~args (fn [] ~@body)))
