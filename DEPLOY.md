# Deployment

How a commit gets from master to https://ww86.eu, and how every PR gets its preview. DNS records, the
open `www` item and the rollback: [DNS.md](DNS.md).

## Pipeline

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

The same machinery as the blog's, `scripts/publish-pages` and its test are copied from there.

## Rules

- Only `GITHUB_TOKEN`: no secrets, no deploy keys, never `pull_request_target` (it runs untrusted code
  with a write token). `contents: write` sits on the jobs `publish`, `preview` and `cleanup` only, never
  at workflow level.
- Previews exist only for PRs from this repo, not for forks and not for Dependabot: their token is
  read-only. A preview is live about a minute after the job (Pages has to build the branch) and
  disappears when the PR is closed. It is built from the PR merged into master as master stood at the
  PR's last push, so it goes stale when master moves: rebase and push to refresh it.
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

## Checking a preview or production

- A preview is ready when the `preview` job is done *and* Pages has built the `gh-pages` commit it made
  (`gh api repos/kastoestoramadus/ww86.eu/pages/builds/latest`, status `built`), not before.
- **The CDN caches 404s** for a minute or two and ignores the query string, so `?x=1` does not get past it.
  To see what Pages serves right now, percent-encode one character of the path: `/preview/pr-%32/` is a
  cache miss for `/preview/pr-2/`.
- Two pushes to `gh-pages` seconds apart cancel the first Pages build. The builds API reports it as
  `errored`, "Page build failed.", the `pages-build-deployment` run as `cancelled`; only the last build
  counts.

## Gotchas

- `/preview/` is reserved for PR previews: `publish-pages` refuses a site that has that path.
- `robots.txt` keeps crawlers from fetching previews, so they never see the `noindex` meta either; a
  preview URL posted publicly could still show up as a bare link. Share preview links with people.
- Branch-based Pages rebuilds after every push to `gh-pages`, preview or not, and has a soft limit of
  10 builds per hour; a burst of pushes to a PR can delay its preview. Changing the Pages *source* does
  not build the branch: request a build with `gh api -X POST repos/kastoestoramadus/ww86.eu/pages/builds`.
- Saving the custom domain in the Pages settings commits a `CNAME` straight to `gh-pages` ("Delete CNAME",
  "Create CNAME"). The next master publish rewrites it from `static/CNAME`, so a domain change goes there.
- Every publish adds a commit to `gh-pages`; the history only grows (the site limit is 1 GB, previews
  count towards it). Squash it by recreating the branch (recipe above) if it ever matters.
- **A job that fails in seconds with zero steps is an environment problem**, not a build problem: the
  message is in the check-run annotations, not in the logs. It happened with the `github-pages`
  environment, created with a branch policy for `main` while this repo uses `master`.
- Until 2026-09-24 the site was published by `actions/deploy-pages` (Pages build type "workflow"), which
  ignores a `CNAME` file and keeps the domain in the Pages settings only. Such a deployment still wins
  over the branch when it is the newest: during the switch a master push that ran the old workflow
  replaced the site and hid every preview until the next push to `gh-pages`. Never bring `deploy-pages`
  back next to the branch source.
