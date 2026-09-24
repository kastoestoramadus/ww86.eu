# lab/

One directory per artifact, each with its own `index.html` and everything it needs. The `buildSite`
task copies every subdirectory here verbatim into `target/site/lab/`, so a page dropped in works
exactly as it was produced - no build step, no framework, nothing rewritten.

To publish one:

1. `mkdir lab/<slug>` and put `index.html` (plus assets) inside.
2. Add a `LabItem` for it in `core/src/main/scala/eu/ww86/site/Catalog.scala`. Title and blurb are in
   English; if the page is not, set `language` and the card says so.
3. `sbt buildSite` and open `target/site/lab/<slug>/index.html`.

Files directly in `lab/` (like this README) are not copied. Slugs written by the generator are marked
`generated = true` in the catalog and must not also exist as a directory here.

## Pages drafted with an AI assistant

A page from a claude.ai chat was written for the person in that chat. Before it lands here:

- address the reader instead of "you and your interview", and drop mentions of files attached to the chat;
- remove links to claude.ai artifacts - they are private, a visitor gets a login page;
- check every external link and drop or replace the dead ones;
- set `lang` on `<html>`, add a `<meta name="description">` and a link back to `../index.html`;
- put `drafted with Claude` in the card's `tech`, as the lab index promises.

Keep the edits reproducible - a script with exact, match-once replacements over the downloaded artifact
works well and fails loudly when the artifact has changed.
