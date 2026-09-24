# DNS and the custom domain

The hub has been served at https://ww86.eu since 2026-09-24. This file records the setup, the one open
item and the way back.

## Records

| Name | Record | Serves |
|------|--------|--------|
| `ww86.eu` (apex) | `A` 185.199.108.153, .109.153, .110.153, .111.153 and `AAAA` 2606:50c0:8000::153 ... 8003::153 | this repository's Pages site |
| `blog.ww86.eu` | `CNAME` → `kastoestoramadus.github.io` | the Jekyll blog, a different repo. **Do not touch.** |
| `www.ww86.eu` | registrar URL forwarding → `https://ww86.eu` | works over http only, see below |

Pages settings: custom domain `ww86.eu`, certificate `approved`, `https_enforced: true`. Pages publishes
the `gh-pages` branch, which takes its domain from the `CNAME` file in the branch root, copied there from
`static/CNAME` by every publish.

```bash
gh api repos/kastoestoramadus/ww86.eu/pages --jq '{cname, https_enforced, cert: .https_certificate.state}'
```

## Open: `www` over https

The registrar's forwarder does not listen on port 443, so `https://www.ww86.eu` times out while
`http://www.ww86.eu` redirects fine. Browsers that try https first stall on it.

The fix is to hand `www` to GitHub as well - replace the URL forwarding with:

```plaintext
www.ww86.eu.  CNAME  kastoestoramadus.github.io.
```

GitHub routes by the `Host` header, not by the CNAME target, so sharing that target with `blog.ww86.eu`
is fine. Checked on 2026-09-24 against GitHub's servers before changing any DNS:

```bash
curl -sI --resolve www.ww86.eu:80:185.199.108.153 http://www.ww86.eu/     # 301 → https://ww86.eu/
curl -sI --resolve nope-xyz.ww86.eu:80:185.199.108.153 http://nope-xyz.ww86.eu/   # 404, control
```

GitHub issues the certificate for `www` only after the record points at it, so https on `www` shows up
some minutes after the change. Check with:

```bash
curl -sI https://www.ww86.eu | grep -iE '^HTTP|^location'   # expect 301 → https://ww86.eu/
```

## Verifying the site

```bash
curl -sI https://ww86.eu | head -1
curl -s https://ww86.eu/lab/digits/index.html | grep -o 'js/main.js'
curl -sI http://ww86.eu | grep -i '^location'                 # → https://ww86.eu/
curl -sIL https://blog.ww86.eu/ww86.eu/ | grep -i '^location' # old default URL → https://ww86.eu/
```

## Rolling back

Not rehearsed since the move to the `gh-pages` branch. Delete `static/CNAME` and the check for it in
`scripts/publish-pages`, merge, then:

```bash
gh api -X PUT repos/kastoestoramadus/ww86.eu/pages --input - <<< '{"cname": null}'
```

Then put the registrar's URL forwarding back on the apex. The hub returns to its default URL,
`https://blog.ww86.eu/ww86.eu/` (project sites live under the account's user site, which owns
`blog.ww86.eu`); all internal links are relative, so it works there unchanged. The blog is unaffected
either way.

To switch back again: `static/CNAME` and its check back, A/AAAA records as in the table, wait until
`getent hosts ww86.eu` shows GitHub's addresses, `gh api -X PUT repos/kastoestoramadus/ww86.eu/pages -f cname=ww86.eu`,
wait for the certificate, then `gh api -X PUT repos/kastoestoramadus/ww86.eu/pages -F https_enforced=true`.
