(ns io.resonant.conjector.trace
  "Tracing application binding process in conjector and libraries on top of it.")

(defonce ^:dynamic *tracing* (atom 0))
(defonce ^:dynamic *tracing-tags* (atom #{:*}))

(defn trace-print [s data]
  (println s (pr-str data)))

(defn trace [level tag msg data]
  (when (and (>= @*tracing* level) (or (@*tracing-tags* tag) (@*tracing-tags* :*)))
     (trace-print (str " [" tag "] " msg) data)))

(defn trace-include [& tags]
  (doseq [tag tags] (swap! *tracing-tags* conj tag)))

(defn trace-exclude [& tags]
  (doseq [tag tags] (swap! *tracing-tags* disj tag)))

