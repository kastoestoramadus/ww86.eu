package eu.ww86.site

class LabItemSuite extends munit.FunSuite:

  private val item = LabItem(slug = "x", title = "Title", blurb = "Blurb", tech = Nil, year = 2026)

  test("every card names the language of its page, English included") {
    assertEquals(item.languageLabel, "In English")
    assertEquals(item.copy(language = Language.Polish).languageLabel, "In Polish")
  }
