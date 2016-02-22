# Architecture

The project is broken down into several package.

* core - Main logic.  You can include just this and the processor if you don't need query support.
* processor - This is the annotation processor.  You'll need it to generate source.
* query - Query builder logic.  Optional.
* tests - The tests.

