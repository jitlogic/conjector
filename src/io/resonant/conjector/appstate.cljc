(ns io.resonant.conjector.appstate
  "Provides a convention and a set of support functions for managing application state."
  (:require
    [io.resonant.conjector :refer [app-component? init shutdown]]
    [io.resonant.conjector.debug :refer [debug]]
    [io.resonant.conjector.process :as proc]))

(def ^:private PROC-ARGS {:proc-node? app-component?})

(defn- extract-pfn [map-fn defval v]
  (or (map-fn (:pdef v)) defval))

(defn app-extract
  "Extracts some data from system definition. This is useful for building config/application
  state schemas, configuration defaults etc."
  [app-def map-fn defval]
  (proc/process
    (assoc PROC-ARGS :proc-fn (partial extract-pfn map-fn defval))
    app-def nil))

(defn- init-pfn [{{:keys [config old-state old-config]} :data,
                  {app-config :config} :all-data,
                  :keys [pdef state app-state] :as v}]
  (debug 90 :conjector.appstate.init-pfn "initialization PFN" {:init-pfn (:path v)})
  (let [rslt (init pdef {:config config, :old-state old-state, :old-config old-config,
                         :app-config app-config :app-state app-state :state state, :init true})]
    (debug 90 :conjector.appstate.init-pfn "initialization result" {:rslt rslt})
    rslt))

(defn app-init
  "Initializes or reloads application state. "
  [app-def config old-state old-config]
  (proc/process
    (assoc PROC-ARGS :proc-fn init-pfn)
    app-def {:config config, :old-state old-state, :old-config old-config}))

(defn- shutdown-pfn [{{:keys [config old-state]} :data
                      {app-config :config} :all-data,
                      :keys [pdef state app-state] :as v}]
  (debug 90 :conjector.appstate.shutdown-pfn "shutdown PFN" {:shutdown-pfn (:path v)})
  (shutdown pdef {:config config, :app-config app-config, :shutdown true,
                  :old-state old-state, :state state :app-state app-state}))

(defn app-shutdown
  "Executes :shutdown handler for all components in reverse order."
  [app-def config old-state]
  (proc/process
    (assoc PROC-ARGS :proc-fn shutdown-pfn, :proc-order reverse)
    app-def {:config config, :old-state old-state}))
