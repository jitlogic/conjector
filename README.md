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


* [Core Concepts](doc/CONCEPTS.md)

* [Processing](doc/PROCESS.md)

* [Application State](doc/APPSTATE.md)

* [Defining Components](doc/COMPONENT.md)

## License

Copyright (c) 2020 Rafał Lewczuk

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
