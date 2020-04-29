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
   :config - component configuration
   :old-config - old component configuration
   :app-config - full configuration
   :state - component state (built so far)
   :old-state - old component state
   :app-state - full application state (all components built so far);
   :init - true when in initialization/reload mode;
   :shutdown - true when in shutdown mode;
   "
  (:require
    [io.resonant.conjector.trace :refer [trace]]
    [io.resonant.conjector.process :as proc]))

(def PROC-ARGS {:proc-node? :init})

(defn- extract-pfn [map-fn defval v]
  (or (map-fn (:pdef v)) defval))

(defn extract [sysdef map-fn defval]
  "Extracts some data from system definition. This is useful for building config/application
  state schemas, configuration defaults etc."
  (proc/process
    (assoc PROC-ARGS :proc-fn (partial extract-pfn map-fn defval))
    sysdef nil))


(defn- init-pfn [{{:keys [init]} :pdef,
                  {:keys [config old-state old-config]} :data,
                  {app-config :config} :all-data,
                  :keys [state app-state] :as v}]
  (trace 90 :conjector.appstate.init-pfn "initialization PFN" {:init-pfn (:path v), :init-fn? (some? init)})
  (when init
    (init {:config config, :old-state old-state, :old-config old-config, :app-config app-config
           :app-state app-state :state state, :init true})))


(defn init [sysdef config old-state old-config]
  "Initializes or reloads application state. "
  (proc/process
    (assoc PROC-ARGS :proc-fn init-pfn)
    sysdef {:config config, :old-state old-state, :old-config old-config}))


(defn- shutdown-pfn [{{:keys [shutdown]} :pdef,
                      {:keys [config old-state]} :data
                      {app-config :config} :all-data,
                      :keys [state app-state] :as v}]
  (trace 90 :conjector.appstate.shutdown-pfn "shutdown PFN" {:shutdown-pfn (:path v), :shutdown-fn? (some? shutdown)})
  (if shutdown
    (shutdown {:config config, :app-config app-config,
               :old-state old-state, :state state :app-state app-state,
               :shutdown true})
    old-state))


(defn shutdown [sysdef config old-state]
  (proc/process
    (assoc PROC-ARGS :proc-fn shutdown-pfn)
    sysdef {:config config, :old-state old-state}))

