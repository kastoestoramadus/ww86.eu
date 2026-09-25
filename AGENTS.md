# AGENTS.md

Guide for AI coding agents working in this repository. This file holds what every task needs; the rest is
split by topic, so read a topic file only when your task touches it. **Keep them up to date**: a change to
the build, deployment, dependencies or conventions, or a new gotcha, updates the file that owns the topic
in the same change. Each fact lives in one file.

## What this is

Source of https://ww86.eu - Waldemar Wosiński's hub: a landing page, an "about me" section and the
**lab**, one page per thing built. Static output, no server, no database.

The blog is a separate repository (Jekyll) published at https://blog.ww86.eu. This site links to it and
never duplicates its content: writing goes on the blog, working pages go here. Do not touch the blog's
URLs from here either: Disqus threads and RSS GUIDs over there are keyed by URL.

## Read before you touch

| Your task touches | Read |
|-------------------|------|
| Scala code in `core`, `gen` or `web`, or `build.sbt` | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Adding or editing a lab page | [lab/README.md](lab/README.md) |
| The Ursus by train page (`lab/ursus-by-train/`) | [lab/ursus-by-train.md](lab/ursus-by-train.md) |
| The workflows, `scripts/`, `gh-pages`, a preview or production that looks wrong | [DEPLOY.md](DEPLOY.md) |
| The domain, DNS records, `www` | [DNS.md](DNS.md) |
| Opening a PR, or telling the user about one | [PULL_REQUESTS.md](PULL_REQUESTS.md) |

Link topic files, never import them with `@file`: an import loads them into every session again.

## Commands

```bash
sbt test                 # core tests on the JVM and on Scala.js, gen tests on the JVM
sbt buildSite            # whole site into target/site
sbt buildPreview         # the same plus a noindex copy in target/preview, as a PR gets it
sbt "~core/testQuick"    # fast loop while changing logic
python3 -m http.server -d target/site 4001   # serve it on http://127.0.0.1:4001
./scripts/test-publish-pages                 # the deploy script against a local bare repo, 1 second
```

**In a browser**: `playwright-core@1.63.0` from npm in your scratchpad, browsers in `~/.cache/ms-playwright`.
On this WSL unpack `libnspr4 libnss3 libasound2t64` without sudo (`apt-get download`, `dpkg-deb -x <deb>
<scratch>/libs`) and run with `LD_LIBRARY_PATH=<scratch>/libs/usr/lib/x86_64-linux-gnu`. Sites that refuse
curl, WebFetch and the headless shell (Leroy Merlin, Carrefour, Facebook) open in the full
`chromium-1243/chrome-linux64/chrome`, headless, with a desktop user agent and
`--disable-blink-features=AutomationControlled`; `DISPLAY=:0` (WSLg) gives a window if the user must log in.

## Rules for every change

- **English, both code and copy.** Identifiers, comments, commit messages, page text. The one exception
  is a lab page written in another language: the page stays as it is, its card is in English and
  shows the flag of the page's language (`LabItem.language`, files in `static/flags/`).
- **No personal data of third parties**, anywhere, including test fixtures: no real names of private
  people, no readings computed for them. This repository is public.
- **The lab carries no interpretations.** `core` computes numbers and ships no labels for what they mean;
  anything that reads meaning into a result belongs to a different site. See `Highlights`, which takes
  its labels from the caller, and `SpecialSums`, whose groups are bare numbers with no names.
- Shared texts and links live in `eu.ww86.site.Site`; keep them consistent with the blog's `_config.yml`
  and about page (Scala & Big Data engineer, Warsaw, 15+ years, banking/fintech/public sector).
- **Every page reserves its rights.** At its foot the notice `Site.rights` in the page's language with a
  link to `copyright.html`, the terms page (`Pages.copyright`); in its head `<meta name="author">`,
  `<meta name="tdm-reservation" content="1">` and `<link rel="license">`. The generator adds them to its
  pages, a lab page holds them verbatim, and `RightsSuite` fails `sbt test` on a page without them.
  `static/robots.txt` turns away the AI training crawlers; the terms page says so, keep the two in step.
- **All internal links and assets are relative**, built through `At(depth)` in `gen/.../Pages.scala`, so
  the site works at a domain root, under a path prefix and from `file://`. Never hardcode a leading `/`.
- Every pure function in `core` gets a test, which must pass on both platforms. `gen` has JVM-only tests
  for what the generator writes and for the data of lab pages (`RightsSuite`, `UrsusSuite`). All munit.
- Pinned versions live in `build.sbt` and `project/plugins.sbt`; bump them deliberately, one at a time.
- The generated site is `target/site`, the preview `target/preview`; nothing built is committed to
  `master`.
- **Work in a `git worktree` of your own**, made before your first branch change:
  `git worktree add <scratchpad>/<name> -b <branch> origin/master`. Several agent sessions share this
  checkout; a `git checkout` in it switches the branch under all of them, and another session's next
  commit lands on your branch (2026-09-24).
