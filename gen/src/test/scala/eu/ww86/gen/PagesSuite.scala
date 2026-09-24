package eu.ww86.gen

import eu.ww86.site.Catalog

class PagesSuite extends munit.FunSuite:

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
