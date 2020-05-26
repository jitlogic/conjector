(ns io.resonant.conjector.appstate
  "Provides a convention and a set of support functions for managing application state."
  (:require
    [io.resonant.conjector.debug :refer [debug]]
    [io.resonant.conjector.process :as proc]))

(def ^:private PROC-ARGS {:proc-node? :init})

(defn- extract-pfn [map-fn defval v]
  (or (map-fn (:pdef v)) defval))

(defn extract [app-def map-fn defval]
  "Extracts some data from system definition. This is useful for building config/application
  state schemas, configuration defaults etc."
  (proc/process
    (assoc PROC-ARGS :proc-fn (partial extract-pfn map-fn defval))
    app-def nil))

(defn- init-pfn [{{:keys [init]} :pdef,
                  {:keys [config old-state old-config]} :data,
                  {app-config :config} :all-data,
                  :keys [state app-state] :as v}]
  (debug 90 :conjector.appstate.init-pfn "initialization PFN" {:init-pfn (:path v), :init-fn? (some? init)})
  (when init
    (init {:config config, :old-state old-state, :old-config old-config, :app-config app-config
           :app-state app-state :state state, :init true})))

(defn init [app-def config old-state old-config]
  "Initializes or reloads application state. "
  (proc/process
    (assoc PROC-ARGS :proc-fn init-pfn)
    app-def {:config config, :old-state old-state, :old-config old-config}))

(defn- shutdown-pfn [{{:keys [shutdown]} :pdef,
                      {:keys [config old-state]} :data
                      {app-config :config} :all-data,
                      :keys [state app-state] :as v}]
  (debug 90 :conjector.appstate.shutdown-pfn "shutdown PFN" {:shutdown-pfn (:path v), :shutdown-fn? (some? shutdown)})
  (if shutdown
    (shutdown {:config config, :app-config app-config,
               :old-state old-state, :state state :app-state app-state,
               :shutdown true})
    old-state))

(defn shutdown [app-def config old-state]
  (proc/process
    (assoc PROC-ARGS :proc-fn shutdown-pfn)
    app-def {:config config, :old-state old-state}))
