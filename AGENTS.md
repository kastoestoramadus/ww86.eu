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
sbt buildPreview         # the same plus a noindex copy in target/preview, as a PR gets it
sbt "~core/testQuick"    # fast loop while changing logic
python3 -m http.server -d target/site 4001   # serve it on http://127.0.0.1:4001
./scripts/test-publish-pages                 # the deploy script against a local bare repo, 1 second
```

`buildSite` deletes `target/site`, copies `static/` and every subdirectory of `lab/`, links
`web` with `fullLinkJS` into `js/main.js`, runs `eu.ww86.gen.generate` to write the pages, then
`eu.ww86.gen.checkLinks`, which fails the build on a link from the site root (see the pipeline rules).

## Deployment pipeline

```plaintext
this repo, branch master (not protected)
  ├─ .github/workflows/deploy.yml
  │    job build    (read-only token, this is where all the PR's code runs)
  │                 sbt test buildPreview → target/site + target/preview; scripts/test-publish-pages
  │                 → upload-artifact `site` (master) / `preview` (PRs)
  │    job publish  (master only, contents: write)         scripts/publish-pages site       → root of gh-pages
  │    job preview  (PRs from this repo, contents: write)  scripts/publish-pages preview N  → gh-pages preview/pr-N/
  └─ .github/workflows/preview-cleanup.yml (PR closed)     scripts/publish-pages remove N   → deletes preview/pr-N/
       → branch gh-pages: built site only, plus `.nojekyll` and `CNAME`
          → GitHub Pages, "deploy from a branch" (gh-pages, /), custom domain ww86.eu
             → https://ww86.eu/                  production
             → https://ww86.eu/preview/pr-<N>/  one preview per open PR
```

The same machinery as the blog's, `scripts/publish-pages` and its test are copied from there. DNS records,
the open `www` item and the rollback: [DNS.md](DNS.md).

Rules of the pipeline:
- Only `GITHUB_TOKEN`: no secrets, no deploy keys, never `pull_request_target` (it runs untrusted code
  with a write token). `contents: write` sits on the jobs `publish`, `preview` and `cleanup` only, never
  at workflow level.
- Previews exist only for PRs from this repo, not for forks and not for Dependabot: their token is
  read-only. A preview is live about a minute after the job (Pages has to build the branch) and
  disappears when the PR is closed.
- A preview is `target/site` copied as it is, plus `<meta name="robots" content="noindex, nofollow">` on
  every page (`buildPreview`, `eu.ww86.gen.Preview`); `static/robots.txt` disallows `/preview/`. Nothing is
  rebuilt for the other path because every link is relative, and `buildSite` keeps it that way: it fails
  on a link from the site root in any page or stylesheet, `lab/` included (`eu.ww86.gen.Links`), since in
  a preview that link would lead to production. It cannot see URLs built by JavaScript.
- `gh-pages` is written by the workflows only, never by hand. It is an orphan branch of built output; a
  master publish replaces everything in the root except `preview/`, a preview publish touches only its
  `preview/pr-N/`. `CNAME` and `.nojekyll` must be in the root: the domain comes from `static/CNAME`,
  `publish-pages` refuses a site without it and re-creates `.nojekyll`. Recreating the branch from
  scratch (drops all previews, open PRs get theirs back on their next push):
  ```bash
  sbt buildSite
  tmp=$(mktemp -d) && cp -a target/site/. "$tmp" && touch "$tmp/.nojekyll" && cd "$tmp"
  git init -q -b gh-pages && git add -A && git commit -qm "Recreate gh-pages" \
    && git remote add origin git@github.com:kastoestoramadus/ww86.eu.git \
    && git push --force origin gh-pages
  ```
- Concurrency: each publishing job has its own group per target (`pages-publish-master`,
  `pages-preview-pr-N`; the cleanup workflow shares the group of its PR). Not one shared group: a group
  holds one running and one *pending* job, and a newer pending job cancels the older one. Races between
  groups are settled by the fetch-rebase-retry in `publish-pages`, which cannot conflict because the jobs
  touch disjoint paths.

Without the custom domain the site falls back to `https://blog.ww86.eu/ww86.eu/` (project sites live
under the account's user site, which owns `blog.ww86.eu`), and the relative links keep it working there.

## Conventions

- **English, both code and copy.** Identifiers, comments, commit messages, page text. The one exception
  is a lab page written in another language: the page stays as it is, its card is in English and names
  the language through `LabItem.language`.
- **No personal data of third parties**, anywhere, including test fixtures: no real names of private
  people, no readings computed for them. This repository is public.
- **The lab carries no interpretations.** `core` computes numbers and ships no labels for what they mean;
  anything that reads meaning into a result belongs to a different site. See `Highlights`, which takes
  its labels from the caller.
- Shared texts and links live in `eu.ww86.site.Site`; keep them consistent with the blog's `_config.yml`
  and about page (Scala & Big Data engineer, Warsaw, 15+ years, banking/fintech/public sector).
- Adding a lab page: see [lab/README.md](lab/README.md), then add a `LabItem` to
  `core/src/main/scala/eu/ww86/site/Catalog.scala` (newest first). Pages drafted in a chat with an AI
  assistant were written for one reader and need the editing pass described there before they land.
- **Not every artifact belongs in the lab.** Screen claude.ai artifacts before proposing them: nothing
  that locates the author's home, nothing from job applications or interview processes, nothing copied
  from someone else's published work.
- **All internal links and assets are relative**, built through `At(depth)` in `gen/.../Pages.scala`, so
  the site works at a domain root, under a path prefix and from `file://`. Never hardcode a leading `/`.
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
- **ScalaTags name clashes**: `title`, `main`, `nav` and `section` are in `scalatags.Text.tags2`, not in
  `all`; `footer` is in neither, hence `tag("footer")` - see the top of `gen/.../Pages.scala`.
- **Locale-sensitive `toUpperCase`** differs between the JVM and JS engines; go through `Words.normalise`
  and keep the cross-platform test that covers Polish letters.
- Do not touch the blog's URLs from here, and do not add a second copy of the blog's content: Disqus
  threads and RSS GUIDs over there are keyed by URL.
- The generated site is `target/site`, the preview `target/preview`; nothing built is committed to
  `master`.
- `/preview/` is reserved for PR previews: `publish-pages` refuses a site that has that path.
- `robots.txt` keeps crawlers from fetching previews, so they never see the `noindex` meta either; a
  preview URL posted publicly could still show up as a bare link. Share preview links with people.
- Branch-based Pages rebuilds after every push to `gh-pages`, preview or not, and has a soft limit of
  10 builds per hour; a burst of pushes to a PR can delay its preview. Changing the Pages *source* does
  not build the branch: request a build with `gh api -X POST repos/kastoestoramadus/ww86.eu/pages/builds`.
- Every publish adds a commit to `gh-pages`; the history only grows (the site limit is 1 GB, previews
  count towards it). Squash it by recreating the branch (recipe above) if it ever matters.
- **A job that fails in seconds with zero steps is an environment problem**, not a build problem: the
  message is in the check-run annotations, not in the logs. It happened with the `github-pages`
  environment, created with a branch policy for `main` while this repo uses `master`.
- Until 2026-09-24 the site was published by `actions/deploy-pages` (Pages build type "workflow"), which
  ignores a `CNAME` file and keeps the domain in the Pages settings only.
