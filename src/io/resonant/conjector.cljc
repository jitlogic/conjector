(ns io.resonant.conjector)

(defprotocol IComponent
  (init [this args])
  (shutdown [this args]))

(defrecord AppComponent [init shutdown]
  IComponent
  (init [_ args]
    (when init (init args)))
  (shutdown [_ args]
    (when shutdown (shutdown args))))

(defn app-component? [obj] (instance? AppComponent obj))

(defn app-component [{:keys [init shutdown] :as m}]
  (merge
    (AppComponent. init shutdown)
    (dissoc m :init :shutdown)))

