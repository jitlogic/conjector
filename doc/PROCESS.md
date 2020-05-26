# State processing

Basic, abstract state processing is implemented in `io.resonant.conjector.process` namespace. 
Function `process` implements generalized construction of such structure based on processing logic, system definition 
and input data:

```clojure
(defn process {:keys [proc-node? proc-fn proc-order requires before] :as proc-args} app-def {:keys [d1,d2,...]:as data} ...)
```

Processing is performed in dependency order, so each processing steps sees results of all previous steps and gets 
guaranteed results of all steps declared as dependencies.

Argument `app-def` is system definition and contains hierachical map that reflects application state and contains
constant definitions needed to construct output structure using functions from `proc-args`. 

Argument `data` contains additional (optional) data items - as there can be multiple sources of data, it is a map
containing maps that reflect structure from `app-def`.

Argument `proc-args` contains map of functions implementing processing general rules:

* `:proc-node?` - accepts node returns true if given node qualifies for component for processing;

* `:proc-fn` - processing function (see below);

* `:proc-order` - accepts paths list processing order, either `identity` or `reverse`

* `:requires` - dependencies (list of paths);

* `:before` - reverse dependencies (list of paths);

While processing, all tree nodes from `app-def` for which `:proc-node?` returns true, are ordered according to `:requires`
and `:before` results and `:proc-order` (reverse or normal). Then for each such node `:proc-fn` function is called with 
results from all previous steps, current node and parts of input data.

Function `:proc-fn` returns part of state and accepts map with following keys:

* `:system` - full state structure (built so far);

* `:path` - path to element currently processed;

* `:state` - local state structure (built so far - `(get-in app-state path)`);

* `:pdef` - processing definition (`(get-in app-defs path)`);

* `:all-data` - all input data

* `:data` - local input data (`get-in all-data path`)

Function result is merged with `app-state` using `path` (`(assoc-in app-state path process-result)`).

On top of this generic mechanism application state management is implemented. 
See [Application State](APPSTATE.md) section for more details.
