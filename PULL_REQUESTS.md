# Pull requests

How to open a PR in this repository and when it counts as done. For why a preview takes time to go live
and how the CDN caches it, see [DEPLOY.md](DEPLOY.md).

## Opening a pull request

A PR is done when its description links a preview that serves the new version, and the user has that
link. In this order:

1. **Open the PR with no preview line.** The number `<N>` exists only once the PR does. Never put a
   placeholder in the body: reviewers see it.
2. **Wait for the preview in the background** (one notification when it ends). Give it the page the PR
   changes and a string that only the new version of that page contains:
   ```bash
   N=<N>; P=lab/<slug>/index.html; MARK='<text only the new version has>'
   tab=$(printf '\t'); res="NOT READY after 20 min: https://ww86.eu/preview/pr-$N/$P"
   for i in $(seq 80); do
     checks=$(gh pr checks "$N" 2>/dev/null)
     if grep -q "${tab}fail${tab}" <<<"$checks"; then res="CHECKS FAILED: gh pr checks $N"; break; fi
     page=$(curl -s -w '\n%{http_code}' "https://ww86.eu/preview/pr-$N/$P")
     if [ -n "$checks" ] && ! grep -q "${tab}pending${tab}" <<<"$checks" \
        && [ "${page##*$'\n'}" = 200 ] && grep -qF "$MARK" <<<"$page"; then
       res="READY https://ww86.eu/preview/pr-$N/$P"; break
     fi
     sleep 15
   done; echo "$res"
   ```
   READY needs three things: the PR's checks have finished with no failure, the link the user will get
   answers 200, and the page contains the marker. A missing preview answers with GitHub's 404 page, which
   has a `<title>` like any other page. A stale preview from an earlier push answers 200, but with the old
   text. Right after a push the checks are not listed yet, so an empty list means "wait". If the PR
   changes no page, use `P=index.html` and `MARK='noindex, nofollow'`: every preview page carries that
   meta tag, and the 404 page does not. After each later push, wait again, with a marker from that push
   if it changes a page.
3. **Put the checked links into the body.** List each page the PR changes next to the same page in
   production, so before and after are one click apart:
   `https://ww86.eu/lab/<slug>/index.html` → `https://ww86.eu/preview/pr-<N>/lab/<slug>/index.html`.
   If the PR changes no page, link `https://ww86.eu/preview/pr-<N>/index.html` and say that the output is
   unchanged. Link only URLs the wait has checked. `/preview/pr-<N>/` without `index.html` is a different
   URL with its own cache entry.
4. **Tell the user about the PR only after that**, in one message with the PR link and the same preview
   links. If the wait printed `CHECKS FAILED` or `NOT READY`, report that, never a link.

## Stacked pull requests

A PR opened on another PR's branch merges into that branch, never into master. Once its base PR is merged,
move it onto master before it is merged itself: the base is squashed, so rebase the stacked branch
(`git rebase --onto origin/master <the base branch's last commit>`), push it and change its base through
REST (`gh api -X PATCH repos/kastoestoramadus/ww86.eu/pulls/<N> -f base=master`). #20 and #21 were merged
into their stacked bases and missed master until a later PR landed them (2026-09-26). When you tell the user
about a stacked PR, say which PR to merge first and that you retarget this one afterwards.

## gh 2.45

- `gh pr checks` has no `--json`. A loop that parses JSON from it gets "unknown flag", and with the error
  silenced it spins until its timeout. Parse the tab-separated plain output, as above.
- `gh pr edit` fails with a GraphQL error about Projects (classic). Edit the body through REST instead:
  `gh api -X PATCH repos/kastoestoramadus/ww86.eu/pulls/<N> -F body=@body.md`.
