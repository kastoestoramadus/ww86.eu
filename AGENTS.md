# AGENTS.md

Guide for AI coding agents working in this repository. **Keep it up to date**: whenever you change the
build, deployment, dependencies, conventions or discover a new gotcha, update this file in the same change.

## What this is

Source of https://ww86.eu - Waldemar Wosiński's hub: a landing page, an "about me" section and the
**lab**, one page per thing built. Static output, no server, no database.

The blog is a separate repository (Jekyll) published at https://blog.ww86.eu. This site links to it and
never duplicates its content: writing goes on the blog, working pages go here.

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

## Commands

```bash
sbt test                 # core tests on the JVM and on Scala.js
sbt buildSite            # whole site into target/site
sbt "~core/testQuick"    # fast loop while changing logic
python3 -m http.server -d target/site 4001   # preview on http://127.0.0.1:4001
```

`buildSite` deletes `target/site`, copies `static/`, every subdirectory of `lab/` and `CNAME`, links
`web` with `fullLinkJS` into `js/main.js`, then runs `eu.ww86.gen.generate` to write the pages.

## Deployment pipeline

```plaintext
master → .github/workflows/deploy.yml → sbt test buildSite → upload-pages-artifact
       → deploy-pages (GITHUB_TOKEN, no secrets) → GitHub Pages, custom domain ww86.eu
```

DNS: apex `ww86.eu` on GitHub Pages A records (185.199.108-111.153); `www.ww86.eu` stays a registrar
redirect to the apex, because the `kastoestoramadus.github.io` host already serves `blog.ww86.eu`.

## Conventions

- **English, both code and copy.** Identifiers, comments, commit messages, page text.
- **No personal data of third parties**, anywhere, including test fixtures: no real names of private
  people, no readings computed for them. This repository is public.
- **The lab carries no interpretations.** `core` computes numbers and ships no labels for what they mean;
  anything that reads meaning into a result belongs to a different site. See `Highlights`, which takes
  its labels from the caller.
- Shared texts and links live in `eu.ww86.site.Site`; keep them consistent with the blog's `_config.yml`
  and about page (Scala & Big Data engineer, Warsaw, 15+ years, banking/fintech/public sector).
- Adding a lab page: see [lab/README.md](lab/README.md), then add a `LabItem` to
  `core/src/main/scala/eu/ww86/site/Catalog.scala` (newest first).
- Every pure function in `core` gets a test. Tests are munit and must pass on both platforms.
- Pinned versions live in `build.sbt` and `project/plugins.sbt`; bump them deliberately, one at a time.

## Gotchas

- **Scala.js has no classpath resources.** `getClass.getResourceAsStream` and `io.Source.fromFile` do not
  exist in the browser: data files stay in JVM-only modules, or get fetched over HTTP.
- **`ModuleKind` is the default `NoModule`**, so `fullLinkJS` yields a single script and `buildSite` copies
  it as `js/main.js`. Switch to `ESModule` only when npm interop is actually needed - it changes the
  linker output into a directory, which `buildSite` already tolerates, and requires `type="module"`.
- **Laminar 17 API**: `onInput.mapToValue --> var`, `child <-- signal`, `cls(name) := boolean`. Older
  snippets (Laminar 0.x) use a different Airstream API and will not compile.
- **ScalaTags name clashes**: `title`, `main`, `nav`, `section`, `footer` are in `scalatags.Text.tags2`,
  not in `all` - see the imports in `gen/.../Pages.scala`.
- **Locale-sensitive `toUpperCase`** differs between the JVM and JS engines; go through `Words.normalise`
  and keep the cross-platform test that covers Polish letters.
- Do not touch the blog's URLs from here, and do not add a second copy of the blog's content: Disqus
  threads and RSS GUIDs over there are keyed by URL.
- The generated site is `target/site`; nothing is committed into `docs/` and Pages is built by the workflow.
