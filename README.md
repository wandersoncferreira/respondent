# Creating your own CES Framework with core.async

Reading the [Clojure Reactive
Programming](https://www.packtpub.com/product/clojure-reactive-programming/9781783986668)
book, we found in Chapter 5 an minimal implementation of a
**Compositional Event Stream (CES)** using core.async, however the code is a bit
outdated e.g. still using `cljx` annotations, deprecated functions
from core.async.

And I also wanted to use Clojure CLI instead of Leiningen in this
project.


Therefore this repository is a simple translation/updated
implementation of what is presented in the Book.

The goals for this framework is to represent [Behaviors](###Behaviors)
and [Event Streams](###Event Streams) and the following operations on
top of them:

Behaviors
- [ ] Create new behaviors
- [ ] Retrieve the current value of a behavior
- [ ] Convert a behavior into an event stream

Event streams
- [ ] Push/deliver a value down the stream
- [ ] Create a stream from a given interval
- [ ] Transform the stream with the `map` and `filter` operations
- [ ] Combine streams with `flatmap`
- [ ] Subscribe to a stream

As extra requirement we want to build a solution that works in Clojure
and ClojureScript.


### Behaviors


### Event Streams


# Development

To run a `clojure nrepl`

`clojure -M:dev:shadow-cljs clj-repl respondent` and connect to the
informed port number.

# Release

To produce a jar file follow the steps:

```shell
clojure -X:jar :jar respondent.jar
```


To test the library in Node.js follow the steps:

```shell
clojure -M:dev:shadow-cljs compile respondent

cd out/respondent/

node

>> Node REPL
>> var x = require('./respondent')
```


### List of changes from original


| original lib         | updated     | why                                    |
|----------------------|-------------|----------------------------------------|
| leiningen            | deps.edn    | I am moving all my work to Clojure CLI |
| cljsbuild & figwheel | shadow-cljs | Build and hot-reload Clojurescript     |
| deftype              | defrecord   | Explain more of why here               |
| core.async/map>      | transducers | Deprecated function                    |

