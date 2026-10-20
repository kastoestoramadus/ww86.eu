package eu.ww86.gen

import eu.ww86.site.{Catalog, Language, Site}

class PagesSuite extends munit.FunSuite:

  /** The markup between an opening tag and its closing tag, so one region is tested on its own. */
  private def region(page: String, open: String, close: String): String =
    val from = page.indexOf(open)
    val to   = page.indexOf(close, from)
    if from < 0 || to < 0 then fail(s"region $open…$close not found") else page.slice(from, to)

  /** Where a region links to, in order: the navigation is an ordered thing. */
  private def hrefs(html: String): List[String] =
    """href="([^"]*)"""".r.findAllMatchIn(html).map(_.group(1)).toList

  test("the hero links the blog and Droga distinctly, then Lab, GitHub and LinkedIn") {
    val hero = region(Pages.landing, """<section class="hero">""", "</section>")
    assertEquals(
      hrefs(hero),
      List(Site.blogUrl, Site.drogaUrl, "lab/index.html", Site.githubUrl, Site.linkedInUrl)
    )
  }

  test("the top navigation links Lab, About, the blog and Droga beside the blog") {
    val top = region(Pages.landing, """<nav class="top">""", "</nav>")
    assertEquals(
      hrefs(top),
      List("index.html", "lab/index.html", "index.html#about", Site.blogUrl, Site.drogaUrl)
    )
  }

  test("the About link row is the secondary location, Droga beside the blog") {
    val about = region(Pages.landing, """<section class="about""", "</section>")
    val row   = region(about, """<p class="links">""", "</p>")
    assertEquals(
      hrefs(row),
      List(Site.blogUrl, Site.drogaUrl, Site.githubUrl, Site.linkedInUrl, Site.contactUrl)
    )
  }

  test("the footer keeps the rights notice and carries one link row: blog, GitHub, terms") {
    val footer = region(Pages.landing, "<footer>", "</footer>")
    assert(footer.contains(Site.rights(Language.English, Site.since)))
    assertEquals(hrefs(footer), List(Site.blogUrl, Site.githubUrl, "copyright.html"))
  }

  test("the landing page has a card for every lab page") {
    val landing = Pages.landing
    val missing = Catalog.items.filterNot(item => landing.contains(s"""<a class="card" href="${item.path}""""))
    assertEquals(missing.map(_.slug), Nil)
  }

  test("the digits page lists the special sums as bare numbers and never names a group") {
    val page = Pages.digits
    assert(page.contains("11 22 33 44 55 66 77 88 99 111 222 · 13 14 16 19 26 · 17 41 · 27"))
    val names = List("karm", "master", "mistrz", "protect", "ochron", "power", "mocy")
    assertEquals(names.filter(page.toLowerCase.contains), Nil)
  }
