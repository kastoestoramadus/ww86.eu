# ww86.eu

Source of [ww86.eu](https://ww86.eu): a landing page and the **lab** - one page per thing I built.
Scala 3 all the way: pure logic in a cross-compiled `core`, static HTML generated with ScalaTags,
interactive bits in Scala.js with Laminar.

```bash
sbt test                                      # core tests, JVM + Scala.js
sbt buildSite                                 # site into target/site
python3 -m http.server -d target/site 4001    # preview
```

The blog lives in a separate repository: [blog.ww86.eu](https://blog.ww86.eu).
Agent-facing notes and conventions: [AGENTS.md](AGENTS.md).
