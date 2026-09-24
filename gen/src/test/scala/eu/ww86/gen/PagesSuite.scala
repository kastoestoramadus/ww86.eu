package eu.ww86.gen

import eu.ww86.site.Catalog

class PagesSuite extends munit.FunSuite:

  test("the landing page has a card for every lab page") {
    val landing = Pages.landing
    val missing = Catalog.items.filterNot(item => landing.contains(s"""<a class="card" href="${item.path}""""))
    assertEquals(missing.map(_.slug), Nil)
  }
