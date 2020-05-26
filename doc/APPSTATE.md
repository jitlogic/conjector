# Application state

On top of processing layer, this one implements application components with init and shutdown logic. 
See `io.resonant.conjector.appstate` namespace. 

Sysdefs nodes have following properties:

* `:init` (mandatory) - init/reload function (see below);

* `:shutdown` (optional) - shutdown function

* `:requires`, `:before` - dependencies and reverse dependencies (as vectors of paths);

All nodes that have non-nil `:init` key will be considered as components. 

Init function can be used for in both initialization and reinitialization scenarios. Function returns part of application
state and accepts a map with following keys:

* `:config` - component configuration;

* `:old-config` - old component configuration;

* `:app-config` - full application configuration;

* `:state` - application state (applicable only for shutdown);

* `:old-state` - old component state;

* `:app-state` - full application state (built so far);

* `:init` - true when in initialization/reload mode;

* `:shutdown` - true when in shutdown mode;

There are three functions implemented in this module. Function `app-init` builds or rebuilds application state by 
initializing/reinitializing all components in proper order. Function `app-shutdown` destroys application state by  
calling shutdown handlers on components (if any) in reverse order.

```clojure
(ns example
  (:require
    [io.resonant.conjector.appstate :as irca]
    [io.resonant.conjector.component :as ircc]))

(def APP-STATE (atom {}))
(def APP-CONF (atom {}))

; This will be suitable for initial configuration and subsequent reloads
(let [conf (read-string (slurp "config.edn"))]
  (reset! APP-STATE (irca/app-init @ircc/APP-DEF conf @APP-STATE @APP-CONF))
  (reset! APP-CONF conf))

; This will shut down application
(irca/app-shutdown @irca/APP-DEF @APP-CONF @APP-STATE)
```

Also, there is function `extract` that can be used to extract custom information from application definition data:

```clojure
(irca/app-extract @irca/APP-DEF (fn [node] (str (:doc node))) "Undocumented")
(irca/app-extract @irca/APP-DEF :config-schema schema.core/Any) 
```

It accepts traverses application definition data and returns results of provided function on component mode combined
into structure resembling application defintion.

