(ns io.resonant.conjector.process
  "Basic application state processing. See documentation of `process` function."
  (:require
    [io.resonant.conjector :refer [*tracing*]]
    [com.stuartsierra.dependency :as dep]))


(defn- path-map-node? [[_ m]]
  (map? m))


(defn- path-map-children [[p m]]
  (for [[k v] m :when (map? v)] [(conj p k) v]))


(defn deps-best [dset dp]
  "Finds best matching dependency from `dset` that satisfies `dp`."
  (let [dpc (count dp)]
    (loop [best nil, [d0 & ds] dset]
      (let [d0c (count d0)]
        (cond
          (= d0 dp) dp
          (and (< d0c dpc) (= (take d0c dp) d0) (or (nil? best) (> d0c (count best)))) (recur d0 ds)
          (some? ds) (recur best ds)
          (nil? best) (throw (ex-info (str "Unsatisfied dependency: " dp " from: " dset) {:dep dp, :dset dset}))
          :else best)))))


(defn deps->exact [deps]
  (let [debest (partial deps-best (map first deps))]
    (for [[n dm] deps
          :let [dr (map debest (:requires dm))]
          :let [db (map debest (:before dm))]]
      [n {:requires dr, :before db}])))


(defn exact->pairs [deps]
  "Converts dependency maps into one-way node pairs representing dependency graph"
  (let [deps (concat
               (for [[p n] deps :let [ds (:requires n)], d ds] [p d])
               (for [[p n] deps :let [ds (:before n)], d ds] [d p]))]
    deps))


(defn pairs->seq [pairs]
  (dep/topo-sort
    (reduce
      #(dep/depend %1 (first %2) (second %2))
      (dep/graph) pairs)))


(defn process-order [{:keys [proc-node? proc-order] :or {proc-order identity} :as proc-args} sysdef]
  (let [nodes (filter #(proc-node? (second %))
                      (tree-seq path-map-node? path-map-children [[] sysdef])),
        pairs (-> nodes deps->exact exact->pairs)
        pairs-seq (pairs->seq pairs)
        pairs-set (set pairs-seq)
        loose-nodes (for [n (map first nodes) :when (not (contains? pairs-set n))]
                      [n (or (:proc-order (get-in sysdef n)) 1000)])
        loose-seq (map first (sort-by second loose-nodes))
        proc-seq (concat loose-seq pairs-seq)]
    (when *tracing*
      (println "[conjector.process] nodes:" (vec (map first nodes)))
      (println "[conjector.process] pairs: " (vec pairs))
      (println "[conjector.process] pairs-seq: " (vec pairs-seq))
      (println "[conjector.process] loose-seq: " (vec loose-seq))
      (println "[conjector.process] proc-seq: " (vec proc-seq)))
    (proc-order proc-seq)))


(defn process [{:keys [proc-fn proc-order] :or {proc-order identity} :as proc-args} sysdef data]
  "Processes input data using rules defined in `sysdef` and processing functions from `proc-args`.
   Argument `sysdef` is a hierarchical map, `data` is a map of input maps that will be used when
   assembling resulting application state. Looks in `sysdef` for nodes satisfying `proc-node?`,
   then processes them in order calculated from dependencies.
   Structure proc-args has following functions:
    :proc-node? - accepts node returns true if given node qualifies for processing
    :proc-fn - processing function (see below)
    :proc-order - accepts paths list processing order, either `identity` or `reverse`
    :requires - dependencies (list of paths);
    :before - reverse dependencies (list of paths);
   Function proc-fn accepts argument map with following keys:
    :state - local state structure (built so far)
    :all-state - full state structure (built so far)
    :path - path to element currently processed
    :pdef - processing definition
    :data - local input data
    :all-data - all input data"
  (let [proc-seq (process-order proc-args sysdef)]
    (reduce
      (fn [state [proc-fn args]]
        (when *tracing*
          (println "[conjector.process] proc-fn: " (:path args)))
        (assoc-in state (:path args) (proc-fn (assoc args :state (get-in state (:path args)), :all-state state))))
      {}
      (for [path proc-seq]
        [proc-fn
         {:path      path
          :pdef      (get-in sysdef path)
          :proc-args proc-args
          :data      (into {} (for [[k v] data] {k (get-in v path)}))
          :all-data  data}]))))
