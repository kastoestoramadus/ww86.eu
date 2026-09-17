# DNS and the custom domain

How to move `ww86.eu` from the registrar's redirect onto this repository's GitHub Pages site, and how to
get back if it goes wrong. Nothing here is urgent: while the custom domain is unset, GitHub serves the
hub on its default URL (see the last section).

## Where things stand

| Name | Now | Note |
|------|-----|------|
| `blog.ww86.eu` | `CNAME` → `kastoestoramadus.github.io` | the Jekyll blog, a different repo. **Do not touch.** |
| `ww86.eu` (apex) | `A` → `143.198.68.197` | the registrar's URL forwarding, which redirects to the blog |
| `www.ww86.eu` | registrar URL forwarding | redirects to the blog as well |

Verified on 2026-09-17 with `python3 -c "import socket; print(socket.gethostbyname_ex('ww86.eu'))"`.

## The switch

**1. Lower the TTL** on the apex records to 600 seconds and wait for the old TTL to expire. Skippable,
but it makes a rollback take minutes instead of hours.

**2. At the registrar**, remove the URL forwarding for the apex and add GitHub Pages' addresses:

```plaintext
ww86.eu.  A  185.199.108.153
ww86.eu.  A  185.199.109.153
ww86.eu.  A  185.199.110.153
ww86.eu.  A  185.199.111.153
```

Optionally the same over IPv6:

```plaintext
ww86.eu.  AAAA  2606:50c0:8000::153
ww86.eu.  AAAA  2606:50c0:8001::153
ww86.eu.  AAAA  2606:50c0:8002::153
ww86.eu.  AAAA  2606:50c0:8003::153
```

Leave `www.ww86.eu` as a registrar redirect, pointing it at `https://ww86.eu` instead of the blog.
GitHub's own apex-plus-`www` pairing expects `www` to be a `CNAME` to `kastoestoramadus.github.io`, and
that host already answers for `blog.ww86.eu`, so the registrar redirect keeps the two apart.

**3. Wait for propagation**, then check that the apex resolves to GitHub:

```bash
getent hosts ww86.eu
```

**4. Set the custom domain** on this repository. A site published by a workflow ignores the `CNAME`
file in the artifact, so this has to be a settings change:

```bash
gh api -X PUT repos/kastoestoramadus/ww86.eu/pages -f cname=ww86.eu
```

**5. Wait for the certificate.** GitHub issues one automatically once the DNS check passes, usually in
minutes:

```bash
gh api repos/kastoestoramadus/ww86.eu/pages --jq '{cname, status, cert: .https_certificate.state}'
```

**6. Enforce HTTPS** once the certificate state is `approved`:

```bash
gh api -X PUT repos/kastoestoramadus/ww86.eu/pages -F https_enforced=true
```

**7. Verify**, and mind that the browser may have cached the old redirect:

```bash
curl -sI https://ww86.eu | head -3
curl -s https://ww86.eu/lab/digits/index.html | grep -o 'js/main.js'
```

## Rolling back

```bash
gh api -X PUT repos/kastoestoramadus/ww86.eu/pages --input - <<< '{"cname": null}'
```

Then restore the registrar's URL forwarding for the apex. The hub returns to the default URL below, and
`blog.ww86.eu` is unaffected either way.

## The default URL, with no custom domain

Project sites live under the account's user site. That site has its own custom domain, so the hub is at:

```plaintext
https://blog.ww86.eu/ww86.eu/
```

`https://kastoestoramadus.github.io/ww86.eu/` redirects there. Every internal link and asset in the
generated pages is relative, so the site works unchanged under that path prefix - but it is served from
the blog's hostname, which is a reason not to hand that link around.

If you want a real URL before committing the apex, a subdomain costs one DNS record and no redirect:

```plaintext
hub.ww86.eu.  CNAME  kastoestoramadus.github.io.
```

followed by `gh api -X PUT repos/kastoestoramadus/ww86.eu/pages -f cname=hub.ww86.eu`. The apex keeps
redirecting to the blog until you are ready.
