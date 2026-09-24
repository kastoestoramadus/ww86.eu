package eu.ww86.site

class LabItemSuite extends munit.FunSuite:

  private val item = LabItem(slug = "x", title = "Title", blurb = "Blurb", tech = Nil, year = 2026)

  test("a page in English needs no language note") {
    assertEquals(item.languageNote, None)
  }

  test("a page in another language says which, since its card is in English") {
    assertEquals(item.copy(language = Language.Polish).languageNote, Some("In Polish"))
  }
