(ns io.resonant.conjector.component
  "Easy to use component definition macro.")

(defn- merge-parse-results [m1 m2]
  {:args (merge (:args m1) (:args m2)),
   :requires (concat (:requires m1) (:requires m2))})

(defn parse-args-map [path args]
  (reduce
    merge-parse-results
    (for [[k v] args]
      (cond
        (and (map? k) (keyword? v))
        (let [rslt (parse-args-map (conj path v) k)]
          {:args {k v}, :requires (:requires rslt)})
        (keyword? v) {:args {k v}, :requires [(conj path v)]}
        (= k :keys) {:args {k v}, :requires (for [s v] (conj path (keyword (name s))))}
        (= :as k) {:args {k v}}
        :else {:args {k v}, :requires [(conj path v)]}))))


(defn parse-component-bindings [bindings]
  (let [args (first bindings)]
    (cond
      (symbol? args) {:args args, :requires []}
      (map? args)
      (let [{:keys [args requires]} (parse-args-map [] args)]
        {:args args, :requires (for [[r0 & rs] requires :when (= :system r0)] (vec rs))})
      :else (ex-info (str "Not proper bindings: " (pr-str bindings)) {:bindings bindings}))))


(defn parse-component-args [args]
  (let [[cdef args] (if (string? (first args)) [{:doc (first args)} (rest args)] [{} args])]
    (merge
      cdef
      (loop [cur args, rslt {}]
        (cond
          (keyword? (first cur)) (recur (drop 2 cur) (assoc rslt (first cur) (second cur)))
          (empty? cur) rslt
          :else (assoc rslt :init cur))))))


(defmacro component [bindings & cargs]
  "Creates a component. Automatically attaches dependency information and additional information."
  (let [{:keys [args] :as pb} (parse-component-bindings bindings),
        {:keys [init init-fn shutdown shutdown-fn] :as pa} (parse-component-args cargs)
        rq (vec (concat (:requires pa) (:requires pb)))]
    (merge
      (dissoc pa :init :init-fn :shutdown :shutdown-fn)
      (when init {:init `(fn [~args] ~@init)})
      (when init-fn {:init init-fn})
      (when shutdown {:shutdown `(fn [~args] ~@shutdown)})
      (when shutdown-fn {:shutdown shutdown-fn})
      (when rq {:requires rq})
      )))

