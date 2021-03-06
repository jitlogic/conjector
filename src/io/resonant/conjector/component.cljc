(ns io.resonant.conjector.component
  "Easy to use component definition macro."
  (:require
    [io.resonant.conjector :refer [app-component]]
    [io.resonant.conjector.util :as cu]))

(defn- merge-parse-results [m1 m2]
  {:args (merge (:args m1) (:args m2)),
   :requires (concat (:requires m1) (:requires m2))})

(defn- parse-args-map [path args]
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

(defn- parse-component-bindings [bindings]
  (let [args (first bindings)]
    (cond
      (symbol? args) {:args args, :requires []}
      (map? args)
      (let [{:keys [args requires]} (parse-args-map [] args)]
        {:args args, :requires (for [[r0 & rs] requires :when (= :app-state r0)] (vec rs))})
      (nil? args) {:args '_}
      :else (throw (ex-info (str "Not proper bindings: " (pr-str bindings)) {:bindings bindings})))))

(defn parse-component-args
  "Parses component arguments. Identifies and groups init code when necessary."
  [args]
  (let [[cdef args] (if (string? (first args)) [{:doc (first args)} (rest args)] [{} args])]
    (merge
      cdef
      (loop [cur args, rslt {}]
        (cond
          (keyword? (first cur)) (recur (drop 2 cur) (assoc rslt (first cur) (second cur)))
          (empty? cur) rslt
          :else
          (do
            (when (some? (:init rslt)) (throw (ex-info "init code declared as both :init key and body" {})))
            (assoc rslt :init (if (= 1 (count cur)) (first cur) (cons 'do cur)))))))))

(defmacro component
  "Creates a component. Automatically attaches dependency information and additional information."
  [bindings & cargs]
  (let [{:keys [args] :as pb} (parse-component-bindings bindings),
        {:keys [init init-fn shutdown shutdown-fn before requires] :as pa} (parse-component-args cargs)
        rq (vec (map cu/to-path (concat requires (:requires pb))))
        m (merge
            (dissoc pa :init :init-fn :shutdown :shutdown-fn)
            (when init {:init `(fn [~args] ~init)})
            (when init-fn {:init init-fn})
            (when shutdown {:shutdown `(fn [~args] ~shutdown)})
            (when shutdown-fn {:shutdown shutdown-fn})
            (when before {:before (vec (map cu/to-path before))})
            (when rq {:requires rq}))]
    `(app-component ~m)))

(defonce APP-DEF (atom {}))

(defonce APP-DEVMODE
  (atom
    {:changes-num 0,
     :changes-ack 0,
     :nscheck-fn nil,
     :reload-fn nil}))

(defn defcomponent* [path component]
  (swap! APP-DEF assoc-in path component)
  (swap! APP-DEVMODE update :changes-num inc))

(defmacro defcomponent [name bindings & args]
  (if (symbol? name)
    `(def ~name (defcomponent* (cu/to-path '~name) (component ~bindings ~@args)))
    `(defcomponent* (cu/to-path '~name) (component ~bindings ~@args))))

