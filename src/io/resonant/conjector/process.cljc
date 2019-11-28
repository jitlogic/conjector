(ns io.resonant.conjector.process
  (:require
    [com.stuartsierra.dependency :as dep]))


(defn- path-map-node? [[_ m]]
  (map? m))


(defn- path-map-children [[p m]]
  (for [[k v] m :when (map? v)] [(conj p k) v]))


(defn dep-pairs [{:keys [requires before]} nodes]
  (let [deps (concat
               (for [[p n] nodes :let [ds (requires n)], d ds] [p d])
               (for [[p n] nodes :let [ds (before n)], d ds] [d p]))]
    deps))


(defn deps-seq [pairs]
  (dep/topo-sort
    (reduce
      #(dep/depend %1 (first %2) (second %2))
      (dep/graph) pairs)))


(defn process [{:keys [proc-node? proc-fn proc-order] :or {proc-order identity} :as proc-args} procdef data]
  "Processes input data using rules defined in `procdef` and processing functions from `proc-args`.
   Argument `procdef` is a hierarchical map, `data` is a map of input maps that will be used when
   assembling resulting application state. Looks in `procdef` for nodes satisfying `proc-node?`,
   then processes them in order calculated from dependencies.
   Structure proc-args has following functions:
    :proc-node? - accepts node returns true if given node qualifies for processing
    :proc-fn - processing function (see below)
    :proc-order - accepts paths list processing order, either `identity` or `reverse`
    :requires - dependencies (list of paths);
    :before - reverse dependencies (list of paths);
   Function proc-fn accepts argument map with following keys:
    :state - state structure (built so far)
    :all-state - full state structure (built so far)
    :path - path to element currently processed
    :pdef - processing definition
    :data - input data related to currently processed element
    :all-data - all input data
   "
  (let [nodes (filter #(proc-node? (second %))
                      (tree-seq path-map-node? path-map-children [[] procdef]))
        proc-seq (-> (dep-pairs proc-args nodes) deps-seq)]
    (reduce
      (fn [state [proc-fn args]]
        (assoc-in state (:path args) (proc-fn (assoc args :state (get-in state (:path args)), :all-state state))))
      {}
      (for [path proc-seq]
        [proc-fn
         {:path path
          :pdef (get-in procdef path)
          :proc-args proc-args
          :data (into {} (for [[k v] data] {k (get-in v path)}))
          :all-data data}]
        ))))

