(ns io.resonant.conjector.debug
  "Tracing application binding process in conjector and libraries on top of it.")

(defonce ^:dynamic *debug* (atom 0))
(defonce ^:dynamic *debug-tags* (atom #{:*}))

(defn debug-print [s data]
  (println s (pr-str data)))

(defn debug [level tag msg data]
  (when (and (>= @*debug* level) (or (@*debug-tags* tag) (@*debug-tags* :*)))
    (debug-print (str " [" tag "] " msg) data)))

(defn debug-include [& tags]
  (doseq [tag tags] (swap! *debug-tags* conj tag)))

(defn debug-exclude [& tags]
  (doseq [tag tags] (swap! *debug-tags* disj tag)))

