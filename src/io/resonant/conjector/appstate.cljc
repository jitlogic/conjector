(ns io.resonant.conjector.appstate
  "Provides a convention and a set of support functions for managing application state. Application state
  is a hierarchical data structure (map) containing initialized runtime components. Application state is built
  from system definition, configuration data and old state (if any).

  Application state definition node contains following keys:
   :init - mandatory init/reload function
   :shutdown - shutdown function
   :requires - dependencies
   :before - reverse dependencies

  Reload function return its part of application state and accepts map with following keys:
   :config - configuration
   :state - application state (built so far)
   :old-state - old application state
   :delayed - reference to delayed execution function that access two arguments: delay (milliseconds), function
   "
  (:require
    [io.resonant.conjector :refer [*tracing*]]
    [io.resonant.conjector.process :as proc]))

(def PROC-ARGS {:proc-node? :init})

(defn- extract-pfn [map-fn defval v]
  (when *tracing*
    (println "[conjector.appstate] extract-pfn:" (:path (:pdef v))))
  (or (map-fn (:pdef v)) defval))

(defn extract [sysdef map-fn defval]
  "Extracts some data from system definition. This is useful for building config/application
  state schemas, configuration defaults etc."
  (proc/process
    (assoc PROC-ARGS :proc-fn (partial extract-pfn map-fn defval))
    sysdef nil))


(defn- init-pfn [{{:keys [init]} :pdef, {:keys [config old-state]} :data, :keys [proc-args state all-state] :as v}]
  (when *tracing*
    (println "[conjector.appstate] init-pfn:" (:path v) "init-fn?" (some? init)))
  (when init
    (init {:config config, :old-state old-state, :delayed (:delayed proc-args), :system all-state :state state, :init true})))

(defn init [sysdef config old-state & {:keys [delayed]}]
  "Initializes or reloads application state. "
  (proc/process
    (assoc PROC-ARGS :proc-fn init-pfn, :delayed delayed)
    sysdef {:config config, :old-state old-state}))


(defn- shutdown-pfn [{{:keys [shutdown]} :pdef {:keys [config old-state]} :data :keys [state all-state] :as v}]
  (when *tracing*
    (println "[conjector.appstate] shutdown-pfn:" (:path v) "shutfown-fn?" (some? shutdown)))
  (if shutdown
    (shutdown {:config config, :old-state old-state, :state state :system all-state, :shutdown true})
    old-state))

(defn shutdown [sysdef config old-state]
  (proc/process
    (assoc PROC-ARGS :proc-fn shutdown-pfn)
    sysdef {:config config, :old-state old-state}))

