# Basic Concepts

Conjector is built around concept that application state is represented as singletree-like structure (map of maps), 
where each component can be accessed using path which is a sequence of keys into subsequent maps. This document will
dissect sample application state along other structures support it and explain core concepts using this as example.

```clojure
{:frob frobnication-cycle-fn
 :db {
   :main main-db-conn-pool
 }
 :web {
   :server http-server-object
   :server-handler main-server-handler-wrapped
   :handlers {
     "/foo" foo-handler-fn
     "/bar" bar-handler-fn
   }
 }
}
``` 

Various object in above structure are addressed by paths, for example: `[:db :main]`, `[:web :handlers "/foo]` and
each part can be accessed using `(get-in app-state [:path :to :component])`. Application state can store constructed
objects (eg. database connections, http server object) or configured functions (closures), for example ring handlers.
Note that various objects in application state can depend on other objects, for example request handlers depend on
database connection, main request handler depends on path handlers, server depends on main request handler. 

Application state is constructed from two data structures: configuration data and another structure that describes
how application state should be built. In Conjector this structure is called application definition. Both configuration 
data and system definition structures generally resemble application state in that they share some parts of structure,
for example configuration would look like this:

```clojure
{:frob {:frob-mode :twiddle}
 :db {
  :main {
    :url "jdbc:h2:mem:test"
    :classname "org.h2.Driver"}
  }
  :web {
    :server {
      :port 3000
      :join? false
    }
    :server-handler {
      :wrappers [:wrap-cookies :wrap-session :wrap-params :wrap-keyword-params]
    }
    :handlers {
      "/foo" {:roles #{:viewer :admin}}
      "/bar" {:roles #{:admin}}
    }
  }
}
```

Application definition is even more interesting. It contains constructors that will create desired parts of application 
state and additional information that will help determine dependencies between constructed components. For example:

```clojure
{:frob {:init frob-init-fn, :requires [[:db :main]]}
 :db {
   :main {:init db-init-fn}
 }
 :web {
   :server {:init web-server-init-fn, :requires [[:web :server-handler]]}
   :server-handler {:init web-handler-init-fn :rquires [[:web :handlers]]}
   :handlers {
     "/foo" {:init foo-handler-init-fn :requires [[:db :main] [:frob]]}
     "/bar" {:init bar-handler-init-fn :requires [[:db :main]]}
   }
 }
}
```

Note that all above structures share common subset:

```clojure
{:frob ...
 :db {
   :main ...
  }
  :web {
   :server ...
   :server-handler ...
   :handlers {
     "/foo" ...
     "/bar" ...
   }
 }
}
```

Leaf nodes of above subset are components of your application. Each subtree under such a node is component definition.
Note that now components can be decoupled using only dependency information (`:requires`) for binding them together. 
Also, additional attributes can be added to application definition extending this mechanism in various ways, for example
`:config-schema` can provide schema for configuration data of individual components, so application configuration can be
validated after binding parts of schema from all components into single data structure. Conjector provides binding/extract
function for such cases.


## Representing paths

Paths are internally represented by vectors but it is not always convenient to use, so alternative forms are available
for developer, where path is represented by keyword, symbol or string where character `:` is used path separator. For
example `[:web :wrappers :auth :oauth2]` is the same as `:web:wrappers:auth:oauth2` or `web:wrappers:auth:oauth2` or
`"web:wrappers:auth:oauth2"`. In various parts those alterantive representations can be used, for example keywrods
are commonly used in configuration data (if it has to refer to components) and symbol form is used in component definitio
macros (see [COMPONENTS](COMPONENT.md)).


## Live reload

Maintaining application state in single structure makes live reload possible and safe. In such cases in addition to 
application definition and configuration data, old application state and old configuration will be passed to 
constructor functions. This allows constructor functions control reload process, for example database connections 
can be recreated if something in their configuration has changed and left unmodified when configuration is exactly 
the same.   

