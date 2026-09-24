# Architecture

How the code is laid out and what bites when changing it. The rules for every change are in
[AGENTS.md](AGENTS.md).

## Stack and why

| Module | Platform | Role |
|--------|----------|------|
| `core` | JVM + Scala.js (`crossProject`, `CrossType.Pure`) | pure logic (`eu.ww86.digits`) and the site model (`eu.ww86.site`) |
| `gen`  | JVM | ScalaTags generator: writes the static HTML at build time |
| `web`  | Scala.js | Laminar widgets, one script for the whole site |

Pages are **generated on the JVM**, not rendered in the browser: they work without JavaScript and are
indexable. Scala.js is used only where a page is interactive, and the widget mounts itself only if its
container exists (`web/.../Main.scala`). `core` is shared, so page metadata and the arithmetic have one
definition and one set of tests, run on both platforms.

## How the site is assembled

`buildSite` deletes `target/site`, copies `static/` and every subdirectory of `lab/`, links
`web` with `fullLinkJS` into `js/main.js`, runs `eu.ww86.gen.generate` to write the pages, then
`eu.ww86.gen.checkLinks`, which fails the build on a link from the site root (see [DEPLOY.md](DEPLOY.md)).

## Gotchas

- **Scala.js has no classpath resources.** `getClass.getResourceAsStream` and `io.Source.fromFile` do not
  exist in the browser: data files stay in JVM-only modules, or get fetched over HTTP.
- **`ModuleKind` is the default `NoModule`**, so `fullLinkJS` yields a single script and `buildSite` copies
  it as `js/main.js`. Switch to `ESModule` only when npm interop is actually needed - it changes the
  linker output into a directory, which `buildSite` already tolerates, and requires `type="module"`.
- **Laminar 17 API**: `onInput.mapToValue --> var`, `child <-- signal`, `cls(name) := boolean`. Older
  snippets (Laminar 0.x) use a different Airstream API and will not compile.
- **ScalaTags name clashes**: `title`, `main`, `nav` and `section` are in `scalatags.Text.tags2`, not in
  `all`; `footer` is in neither, hence `tag("footer")` - see the top of `gen/.../Pages.scala`.
- **Locale-sensitive `toUpperCase`** differs between the JVM and JS engines; go through `Words.normalise`
  and keep the cross-platform test that covers Polish letters.
