(ns io.resonant.conjector.utils)

(defn to-path [v]
  (cond
    (vector? v) v
    (symbol? v) (vec (map keyword (.split (name v) ":")))
    (string? v) (vec (map keyword (.split v ":")))
    (keyword? v) [v]
    :else (throw (ex-info (str "Cannot convert to path: " v) {:arg v}))))

(defn null-wrapper [_ f] (f))

(defmacro with-wrapper [wrapper args & body]
  `((or ~wrapper null-wrapper) ~args (fn [] ~@body)))
