package eu.ww86.gen

import eu.ww86.digits.SpecialSums
import eu.ww86.site.{Catalog, LabItem, Language, Site}
import scalatags.Text.all.*
import scalatags.Text.tags2.{main as mainTag, nav, section, title as titleTag}

private val footerTag = tag("footer")

/** Where a page sits in the site, so every internal link can be relative.
  *
  * Relative links keep the site working at a domain root, under a path prefix (which is what GitHub
  * Pages serves before the custom domain is switched on) and straight from `file://`.
  */
private final case class At(depth: Int):
  /** `path` is given from the site root, without a leading slash. */
  def apply(path: String): String = "../" * depth + path

/** Every page of the hub. Plain HTML: the pages work without JavaScript, widgets are opt-in. */
object Pages:

  /** The share card every page points its Open Graph image at; `static/share.svg` is its source. */
  private val shareImage = s"https://${Site.domain}/share.png"
  private val shareAlt   = s"The ${Site.domain} share card: ${Site.author}, ${Site.tagline}"

  def landing: String =
    val at = At(0)
    layout("Waldemar Wosiński", Site.description, at, canonical = "")(
      section(cls := "hero")(
        h1(Site.author),
        p(cls := "tagline")(Site.tagline),
        p(cls := "links")(
          a(href := Site.blogUrl)("Blog"),
          a(href := Site.drogaUrl)("Droga"),
          a(href := at("lab/index.html"))("Lab"),
          a(href := Site.githubUrl)("GitHub"),
          a(href := Site.linkedInUrl)("LinkedIn")
        ),
        heroPlate
      ),
      section(cls := "cards-section")(
        h2("From the lab"),
        p(cls := "muted")("Small things I built, each one a page you can open."),
        div(cls := "cards")(Catalog.items.map(card(_, at)))
      ),
      aboutSection
    )

  def labIndex: String =
    val at = At(1)
    layout("Lab", "Demos, tools and experiments by Waldemar Wosiński.", at, canonical = "lab/")(
      section(cls := "cards-section")(
        h1("Lab"),
        p(cls := "muted")("Working pages rather than screenshots."),
        div(cls := "cards")(Catalog.items.map(card(_, at)))
      )
    )

  def digits: String =
    val at   = At(2)
    val item = Catalog.items.find(_.slug == "digits").get
    layout(item.title, item.blurb, at, withScript = true, canonical = "lab/digits/")(
      section(cls := "page")(
        p(cls := "breadcrumb")(a(href := at("lab/index.html"))("← Lab")),
        h1(item.title),
        p(cls := "muted")(item.blurb),
        div(id := "digits-widget")(
          p(cls := "muted")("Loading the calculator…")
        ),
        h2("How it works"),
        p(
          "Each letter carries a digit from the 1-9 cycle, with Polish diacritics folded onto their base letter. ",
          "Vowels and consonants are summed separately, then each sum is reduced by repeated digit sum until one digit is left."
        ),
        p(
          "A vowel, consonant or total sum is marked as special when it falls in one of these groups: ",
          SpecialSums.groups.map(_.mkString(" ")).mkString(" · "),
          ". The groups are numbers only; the page does not say what they stand for."
        ),
        p(
          "The second table counts the letters that carry each digit, word by word and for the whole name; ",
          "a 0 marks a digit that no letter carries."
        ),
        h2("Adding a name"),
        p(
          "The search adds one first name to the typed name and keeps the names that give the whole name the chosen roots, ",
          "most common first. The list offers the 81 triples a name can have out of 729: the total root is the root of the other two added up."
        ),
        p(
          "The names are those of living people in the PESEL register as of 20 January 2026, ",
          a(href := "https://dane.gov.pl/pl/dataset/1667")("published on dane.gov.pl"),
          " under CC0, with the number of people who bear each one as the first and as the second name; ",
          "names borne first by fewer than 20 people are left out. Three filters, each one can be switched off, keep to names that read as Polish:"
        ),
        ul(
          li("a minimum number of people who bear the name first;"),
          li(
            "also a second name: at least one person in 20 of those who bear it first has it as the second name. ",
            "This drops transliterations such as OLEKSANDR or TETIANA, common first names in the register and hardly ever second ones; ",
            "of the Polish names it drops TYMON;"
          ),
          li("Polish alphabet: no Q, V or X, which also drops XAWERY and VIOLETTA.")
        ),
        p(
          "The arithmetic lives in a cross-compiled module, so the same code and the same tests run on the JVM and in the browser. ",
          "It carries no interpretation of the numbers: what a sum is supposed to mean is the caller's business, not the calculator's."
        ),
        p(cls := "muted")(s"Built with ${item.tech.mkString(", ")}.")
      )
    )

  def copyright: String =
    val at = At(0)
    layout("Copyright and terms of use", s"Who owns the pages of ${Site.domain} and what you may do with them.", at, canonical = "copyright.html")(
      section(cls := "page")(
        h1("Copyright and terms of use"),
        p(
          s"Everything on ${Site.domain}, the lab pages included, is © ${Site.author}; the year on a page is the year it was first published. ",
          "All rights reserved: copying, republishing or adapting a page, whole or in part, needs my written permission. ",
          "To ask, use the ",
          a(href := Site.contactUrl)("contact on the blog"),
          "."
        ),
        h2("Without asking"),
        p(
          "Link to any page. Some uses need no permission under the law anyway, such as quoting a fragment with its author and source ",
          "(Art. 29 of the Polish Act on Copyright and Related Rights) or private use; nothing here takes them away."
        ),
        h2("Text and data mining"),
        p(
          "Text and data mining of this site is reserved, training AI models included, under Art. 4(3) of Directive (EU) 2019/790 ",
          "and Art. 26³ of the Polish Act on Copyright and Related Rights. ",
          "The reservation is also machine-readable, as Polish law requires for content published online: every page carries ",
          code("""<meta name="tdm-reservation" content="1">"""),
          " (the W3C TDM Reservation Protocol), and ",
          a(href := at("robots.txt"))("robots.txt"),
          " turns away the crawlers that collect training data. ",
          "Mining for scientific research, which the law allows research organisations regardless (Art. 3 of the Directive), is not affected."
        ),
        h2("How the site is made"),
        p("The site and its pages are developed with agentic AI systems, under my direction."),
        h2("Source code"),
        p(
          "The source of this site is public on ",
          a(href := Site.sourceUrl)("GitHub"),
          " to be read, not reused: no open-source license is granted. ",
          "GitHub's terms let its users view a public repository and fork it on GitHub; ",
          "any other use of the code needs permission, like the rest of the site."
        ),
        h2("Other people's work"),
        p(
          "Facts such as timetables, opening hours and addresses are not anyone's to own; each lab page links its sources. ",
          "Work by others keeps its own license: highlight.js under the BSD 3-Clause License, its notice kept in the pages that bundle it; ",
          "fonts from Google Fonts under the SIL Open Font License; the first names on the digits page, from the PESEL register, under CC0; ",
          "the map on the Ursus page is drawn by Leaflet under the BSD 2-Clause License over map tiles from OpenStreetMap contributors, ",
          "under the Open Database License, with the attribution the map itself keeps."
        )
      )
    )

  private def card(item: LabItem, at: At): Frag =
    a(cls := "card", href := at(item.path), attr("hreflang") := item.language.code)(
      glyph(item.slug),
      h3(item.title),
      p(item.blurb),
      p(cls := "tech")(
        item.tech.mkString(" · "),
        span(cls := "meta")(
          img(
            cls   := "flag",
            src   := at(s"flags/${item.language.flag}.svg"),
            alt   := item.languageLabel,
            title := item.languageLabel
          ),
          span(cls := "year")(item.year.toString)
        )
      )
    )

  /** One lab card exactly as the catalog pages show it; public with a plain depth so generator
    * tests can render fixture items (a long title, a language) the same way the pages do.
    */
  def cardMarkup(item: LabItem, depth: Int): String = card(item, At(depth)).render

  /** The hero plate of the illustrated edition: the lab drawn as one ink sketch — an open
    * notebook (its pages carrying the digit pips and the code chevrons), a train of ideas
    * leaving along the ruled edge, stars over the desk. Pure geometry, drawn in the page's
    * own colours; no text, nothing to load, `aria-hidden` keeps it out of the way.
    */
  private val heroPlate: Frag =
    val art =
      """<g fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
        |  <!-- the desk edge the whole sketch stands on, with rail ties beneath -->
        |  <path d="M14 152 C190 149 370 149 546 152"/>
        |  <path d="M330 156 l0 6 M366 156 l0 6 M402 156 l0 6 M438 156 l0 6 M474 156 l0 6 M510 156 l0 6"
        |        stroke-width="2"/>
        |  <!-- open notebook, slightly tilted like a sketch left on the desk -->
        |  <g transform="rotate(-3 140 110)">
        |    <path d="M140 138 C118 127 92 125 66 131 L66 72 C92 66 118 68 140 78 C162 68 188 66 214 72 L214 131 C188 125 162 127 140 138 Z"/>
        |    <path d="M140 78 L140 138" stroke-width="2"/>
        |    <path d="M78 88 C96 84 116 86 130 92 M78 102 C96 98 116 100 130 106 M150 92 C164 86 184 84 202 88 M150 106 C164 100 184 98 202 102" stroke-width="1.6"/>
        |    <!-- digit pips counted onto the left page -->
        |    <circle cx="92" cy="117" r="3.4" fill="currentColor" stroke="none"/>
        |    <circle cx="106" cy="124" r="3.4" fill="currentColor" stroke="none"/>
        |    <circle cx="120" cy="117" r="3.4" fill="currentColor" stroke="none"/>
        |    <!-- code chevrons inked onto the right page -->
        |    <path d="M156 114 L166 121 L156 128 M186 128 L176 121 L186 114" stroke-width="2"/>
        |    <!-- a ribbon marking the page, in the accent ink -->
        |    <path d="M214 96 L228 96 L222 103 L228 110 L214 110" fill="var(--accent)" stroke="none"/>
        |  </g>
        |  <!-- the idea train: chimney, body, wheels, a lit window -->
        |  <g>
        |    <path d="M348 150 L348 112 C348 106 353 102 359 102 L448 102 C455 102 460 107 460 113 L460 132 C460 138 455 142 449 142 L348 142"/>
        |    <rect x="368" y="86" width="12" height="16" rx="3"/>
        |    <path d="M364 74 C370 72 372 66 368 62 M380 70 C386 66 386 60 381 57" stroke-width="2"/>
        |    <circle cx="376" cy="150" r="7"/>
        |    <circle cx="410" cy="150" r="7"/>
        |    <circle cx="440" cy="150" r="7" fill="var(--accent)" stroke="none"/>
        |    <rect x="398" y="112" width="22" height="16" rx="2.5" fill="var(--accent)" stroke="none"/>
        |    <rect x="428" y="112" width="14" height="16" rx="2.5"/>
        |  </g>
        |  <!-- sparks the train leaves behind -->
        |  <path d="M330 116 l-10 5 M332 130 l-12 1" stroke-width="2"/>
        |  <!-- the pips travel from the page to the rails: a dashed arc, circled once by a lens -->
        |  <path d="M226 96 C262 78 296 82 322 100" stroke-width="2" stroke-dasharray="1 8"/>
        |  <circle cx="272" cy="83" r="12"/>
        |  <circle cx="272" cy="83" r="3" fill="currentColor" stroke="none"/>
        |  <!-- stars over the desk, one in gold -->
        |  <path d="M60 44 l3.2 8.4 L72 55 l-8.8 2.6 L60 66 l-3.2 -8.4 L48 55 l8.8 -2.6 Z" fill="currentColor" stroke="none"/>
        |  <path d="M262 34 l2.4 6.4 L271 42.6 l-6.6 2 L262 51 l-2.4 -6.6 L253 44.6 l6.6 -2.2 Z" fill="var(--accent)" stroke="none"/>
        |  <path d="M508 52 l2.8 7.4 L518 62 l-7.2 2.2 L508 72 l-2.8 -7.8 L498 62 l7.2 -2.6 Z" fill="currentColor" stroke="none"/>
        |  <path d="M170 30 C176 26 184 28 186 34" stroke-width="1.8"/>
        |  <path d="M470 40 C476 36 484 38 486 44" stroke-width="1.8"/>
        |</g>""".stripMargin
    div(cls := "plate")(
      raw(s"""<svg aria-hidden="true" focusable="false" viewBox="0 0 560 172">$art</svg>""")
    )

  /** A small decorative spot drawing for a lab page, in the ink of the illustrated edition:
    * hand-drawn geometry rather than a flat mark, still pure geometry — no text to localise,
    * nothing to load, and `aria-hidden` keeps it out of the way. */
  private def glyph(slug: String): Frag =
    val mark = slug match
      case "scala-to-java" =>
        """<path d="M5.5 6.5 C7.8 9.6 10.1 12.4 12.3 15.2 C10.1 17.9 7.8 20.6 5.5 22.9" fill="none"
          | stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
          |<path d="M22.9 6.5 C20.6 9.6 18.3 12.4 16.1 15.2 C18.3 17.9 20.6 20.6 22.9 22.9" fill="none"
          | stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
          |<path d="M8.5 25 C11.5 23.4 16.5 23.4 19.5 25 M19.5 25 l-0.4 -3 M19.5 25 l-3 -0.6" fill="none"
          | stroke="var(--accent)" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
          |<circle cx="14" cy="15" r="2.1" fill="var(--accent)"/>""".stripMargin
      case "digits" =>
        """<path d="M14 4.6 C19 4.2 23.6 8.4 23.5 13.9 C23.4 19.6 18.9 23.6 13.7 23.4 C8.6 23.2 4.5 19.1 4.6 13.9 C4.7 9.1 8.9 4.9 14 4.6 Z"
          | fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round"/>
          |<circle cx="9.6" cy="9.8" r="2.3" fill="currentColor"/>
          |<circle cx="14" cy="14.1" r="2.3" fill="currentColor"/>
          |<circle cx="18.4" cy="18.4" r="2.3" fill="currentColor"/>
          |<circle cx="18.6" cy="9.6" r="2.3" fill="var(--accent)"/>""".stripMargin
      case "ursus-by-train" =>
        """<path d="M3 21.5 C10 21.2 18 21.2 25 21.5" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
          |<path d="M6 21.3 l0 3 M12.5 21.2 l0 3 M19 21.2 l0 3" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          |<path d="M8 17.5 L8 9.5 C8 8.2 9 7.3 10.2 7.3 L18.6 7.3 C19.9 7.3 20.9 8.3 20.9 9.5 L20.9 17.5"
          | fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
          |<rect x="11.2" y="3.4" width="4" height="4" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.9"/>
          |<path d="M9.4 5.6 C11.8 5 12.7 3 11.5 1.6" fill="none" stroke="var(--accent)" stroke-width="1.7" stroke-linecap="round"/>
          |<rect x="13.4" y="10.4" width="4.6" height="3.4" rx="1" fill="var(--accent)"/>
          |<circle cx="11" cy="18.6" r="2.5" fill="currentColor"/>
          |<circle cx="17.8" cy="18.6" r="2.5" fill="currentColor"/>""".stripMargin
      case _ =>
        """<path d="M14 24.4 C10.8 22.6 7.2 22.3 4.2 23 L4.2 8 C7.2 7.3 10.8 7.6 14 9.4 C17.2 7.6 20.8 7.3 23.8 8 L23.8 23 C20.8 22.3 17.2 22.6 14 24.4 Z"
          | fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
          |<path d="M14 9.4 L14 24.4" fill="none" stroke="currentColor" stroke-width="1.8"/>
          |<circle cx="14" cy="17" r="2.1" fill="var(--accent)"/>""".stripMargin
    raw(
      s"""<svg class="glyph" aria-hidden="true" focusable="false" viewBox="0 0 28 28">$mark</svg>"""
    )

  private def aboutSection: Frag =
    section(cls := "about", id := "about")(
      h2("About me"),
      Site.about.map(paragraph => p(paragraph)),
      p(
        "Longer form, and everything I have opinions about, is on ",
        a(href := Site.blogUrl)("blog.ww86.eu"),
        "."
      ),
      p(cls := "links")(
        a(href := Site.blogUrl)("Blog"),
        a(href := Site.drogaUrl)("Droga"),
        a(href := Site.githubUrl)("GitHub"),
        a(href := Site.linkedInUrl)("LinkedIn"),
        a(href := Site.contactUrl)("Contact")
      )
    )

  private def layout(
      pageTitle: String,
      pageDescription: String,
      at: At,
      withScript: Boolean = false,
      canonical: String = ""
  )(sections: Frag*): String =
    val fullTitle = if pageTitle == Site.author then pageTitle else s"$pageTitle · ${Site.domain}"
    doctype("html")(
      html(lang := "en")(
        head(
          meta(charset := "utf-8"),
          meta(name := "viewport", content := "width=device-width, initial-scale=1"),
          titleTag(fullTitle),
          meta(name := "description", content := pageDescription),
          meta(name := "author", content := Site.author),
          meta(name := "tdm-reservation", content := "1"),
          // what a link preview shows; the image is absolute because scrapers resolve no relative
          // URL, everything a browser loads below stays relative and preview-safe
          meta(attr("property") := "og:type", content := "website"),
          meta(attr("property") := "og:site_name", content := Site.domain),
          meta(attr("property") := "og:title", content := fullTitle),
          meta(attr("property") := "og:description", content := pageDescription),
          meta(attr("property") := "og:url", content := s"https://${Site.domain}/$canonical"),
          meta(attr("property") := "og:image", content := shareImage),
          meta(attr("property") := "og:image:width", content := "1200"),
          meta(attr("property") := "og:image:height", content := "630"),
          meta(attr("property") := "og:image:alt", content := shareAlt),
          meta(name := "twitter:card", content := "summary_large_image"),
          link(rel := "license", href := at("copyright.html")),
          link(rel := "icon", href := at("favicon.svg"), `type` := "image/svg+xml"),
          link(rel := "stylesheet", href := at("css/site.css")),
          if withScript then script(src := at("js/main.js"), attr("defer").empty) else frag()
        ),
        body(
          nav(cls := "top")(
            a(cls := "brand", href := at("index.html"))(Site.domain),
            span(cls := "top-links")(
              a(href := at("lab/index.html"))("Lab"),
              a(href := at("index.html#about"))("About"),
              a(href := Site.blogUrl)("Blog"),
              a(href := Site.drogaUrl)("Droga")
            )
          ),
          mainTag(sections),
          footerTag(
            p(Site.rights(Language.English, Site.since)),
            p(cls := "muted")(
              a(href := Site.blogUrl)("blog.ww86.eu"),
              " · ",
              a(href := Site.githubUrl)("GitHub"),
              " · ",
              a(href := at("copyright.html"))("Terms of use")
            )
          )
        )
      )
    ).render
