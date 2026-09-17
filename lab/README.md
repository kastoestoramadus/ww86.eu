# lab/

One directory per artifact, each with its own `index.html` and everything it needs. The `buildSite`
task copies every subdirectory here verbatim into `target/site/lab/`, so a page dropped in works
exactly as it was produced - no build step, no framework, nothing rewritten.

To publish one:

1. `mkdir lab/<slug>` and put `index.html` (plus assets) inside.
2. Add a `LabItem` for it in `core/src/main/scala/eu/ww86/site/Catalog.scala`.
3. `sbt buildSite` and open `target/site/lab/<slug>/index.html`.

Files directly in `lab/` (like this README) are not copied. Slugs written by the generator are marked
`generated = true` in the catalog and must not also exist as a directory here.
