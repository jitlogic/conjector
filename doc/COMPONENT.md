# Components

Components layer in `io.resonant.conjector.component` namespace are syntactic sugar macros on top of functions from
`io.resonant.conjector.appstate`. It is designed to make 


## Manual component creation (not recommended)

Without macros, programmer has to define constructor functions and manually determine dependencies between components.

Constructor functions accept configuration data and application state created so far. It is guaranteed that all required
dependencies are present. Examples:

```clojure
(defn foo-handler-init-fn [{{:keys [frob], {main-db :main} :db} :app-state}]
  (routes
    (GET "/:id" [id]
      (json-response 
        (first (jdbc/query main-db ["select * from widgets where id = ?" id]))))
    (POST "/:id" [id bar]
      (frob id bar))
    (not-found "I see darkness! Darkness I see!")))

(defn frob-init-fn [{{:keys [frob-mode]} :config, {{main-db :main} :db} :app-state}]
  (fn [id arg]
    (when-let [widget (first (jdbc/query main-db ["select * from widgets where id = ?" id]))]
      (json-response (frobnicate widget frob-mode arg)))))
```

Now programmer has to create component description structures and integrate them into application definition:

```clojure
(def FOO-COMPONENT {:init foo-handler-init-fn, :requires [[:frob],[:db :main]]})
(def FROB-COMPONENT {:init frob-init-fn, :requires [[:db :main]]})

(swap! @rcca/APP-DEF assoc-in [:web :handlers "/foo"] FOO-COMPONENT)
(swap! @rcca/APP-DEF assoc-in [:frob] FROB-COMPONENT)
```

This method is quite complicated, so above example is only intended to show mechanics behind component creation.

In practical terms easier way of declaring components is needed and this is where macros come in.


## Using component macros

Conjector maintains global registry of created components that is updated when `defcomponent` macro is used. Let's 
declare `frob` component:

```clojure
(defcomponent frob [{{:keys [frob-mode]} :config, {{main-db :main} :db} :app-state}]
  (fn [id arg]
      (when-let [widget (first (jdbc/query main-db ["select * from widgets where id = ?" id]))]
        (json-response (frobnicate widget frob-mode arg)))))
```

This will create constructor function, component structure and register it in `io.resonant.conjector.component.APP-DEF`.
Also component will be defined in local namespace as `frob`. Note that no dependencies were declared here - these will 
be inferred automatically from arguments.

It is possible to declare component deeper in application definition structure and modify component attributes or even
add custom attributes, for example:

```clojure
(defcomponent db:main [{:keys [config]}]
  :before [[:flyway]]
  :config-schema DB-CONFIG-SCHEMA 
  (next.jdbc.connection/->pool HikariDataSource config))
```

Above example will result in creating component in `[:db :main]`, adds additional reverse dependency using `:before`
and adds custom attribute `:config-schema`. All forms after keyword-value pairs will be treated as code block that
makes up `:init` constructor function.

In some cases components also have to be shut down (`:shutdown` function) and can handle reloads by reusing old state
when configuration did not change. In such cases both `:init` and `:shutdown` code has to be present, for example:

```clojure
(defcomponent db:main [{:keys [config state old-config old-state]}]
  :before [[:flyway]]
  :config-schema DB-CONFIG-SCHEMA
  :init
  (if (= config old-config) 
    old-state                             ; return old state when configuration did not change
    (do
      (when old-state (.close old-state)) ; close old connection and open new when configuration has changed
      (next.jdbc.connection/->pool HikariDataSource config)))
  :shutdown
  (when state (.close state)))
```

Note that additional dependencies will extend but not override inferred ones. Also, it is possible to use path instead
of symbol as first argument - for example `[:web :handlers "/foo"]` - but those will not be defined in local ns. 

There is also macro `component` that works just like `defcomponent` (except that first argument is skipped) that only
creates component structure that can be later used.

```clojure
(def counter-component
  (component [{{:keys [initial]} :config {:keys [counter]} :old-state}]
    :before [[:baz]], :requires [[:bag]]
    :config-schema {:initial s/Num}
    (let [counter (or counter (atom initial))]
      {:counter counter,
       :increment (fn [] (swap! counter inc))})))
```
