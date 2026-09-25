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

  def landing: String =
    val at = At(0)
    layout("Waldemar Wosiński", Site.description, at)(
      section(cls := "hero")(
        h1(Site.author),
        p(cls := "tagline")(Site.tagline),
        p(cls := "links")(
          a(href := Site.blogUrl)("Blog"),
          a(href := at("lab/index.html"))("Lab"),
          a(href := Site.githubUrl)("GitHub"),
          a(href := Site.linkedInUrl)("LinkedIn")
        )
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
    layout("Lab", "Demos, tools and experiments by Waldemar Wosiński.", at)(
      section(cls := "cards-section")(
        h1("Lab"),
        p(cls := "muted")("Working pages rather than screenshots."),
        div(cls := "cards")(Catalog.items.map(card(_, at)))
      )
    )

  def digits: String =
    val at   = At(2)
    val item = Catalog.items.find(_.slug == "digits").get
    layout(item.title, item.blurb, at, withScript = true)(
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
    layout("Copyright and terms of use", s"Who owns the pages of ${Site.domain} and what you may do with them.", at)(
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
          "fonts from Google Fonts under the SIL Open Font License; the first names on the digits page, from the PESEL register, under CC0."
        )
      )
    )

  private def card(item: LabItem, at: At): Frag =
    a(cls := "card", href := at(item.path), attr("hreflang") := item.language.code)(
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
        a(href := Site.githubUrl)("GitHub"),
        a(href := Site.linkedInUrl)("LinkedIn"),
        a(href := Site.contactUrl)("Contact")
      )
    )

  private def layout(pageTitle: String, pageDescription: String, at: At, withScript: Boolean = false)(
      sections: Frag*
  ): String =
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
              a(href := Site.blogUrl)("Blog")
            )
          ),
          mainTag(sections),
          footerTag(
            p(Site.rights(Language.English, Site.since), " ", a(href := at("copyright.html"))("Terms of use")),
            p(cls := "muted")(
              a(href := Site.blogUrl)("blog.ww86.eu"),
              " · ",
              a(href := Site.githubUrl)("GitHub")
            )
          )
        )
      )
    ).render
