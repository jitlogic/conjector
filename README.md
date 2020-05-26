# Conjector

Simple library for managing single application state as map of objects (including maps) with dependency injection,
initialize/shutdown behavior. It is intended to be used to manage single global state structure that can be used as
basis for maintainable multi-component applications. 

[![Clojars Project](https://clojars.org/io.resonant/conjector/latest-version.svg)](https://clojars.org/io.resonant/conjector) 

[![cljdoc badge](https://cljdoc.org/badge/io.resonant/conjector)](https://cljdoc.org/d/io.resonant/conjector)


Library has layered design. The `process` layer contains abstract dependency processing and generalized state structure 
construction in dependency defined order. On top of that there is `appstate` which implements application state managent
as it is used by application itself. The `component` layer implements convenience macro that allows for defining 
components for `appstate` in a more convenient way (kind of DSL).

TBD różnica między conjector a integrant

* [Core Concepts](doc/CONCEPTS.md)

* [Processing](doc/PROCESS.md)

* [Application State](doc/APPSTATE.md)

* [Defining Components](doc/COMPONENT.md)


## License

Copyright © Rafal Lewczuk 2020 rafal.lewczuk@jitlogic.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
