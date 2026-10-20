package eu.ww86.site

/** The catalog is data, so its own shape is tested like logic: the order the pages show and the
  * length of what a card has to carry.
  */
class CatalogSuite extends munit.FunSuite:

  test("newest first: years never increase down the catalog") {
    val years = Catalog.items.map(_.year)
    assertEquals(years, years.sorted.reverse)
  }

  test("every blurb stays scannable on a card: at most three sentences and 360 characters") {
    val tooLong = Catalog.items.filter(item => item.blurb.length > 360 || item.blurb.count(_ == '.') > 3)
    assertEquals(tooLong.map(_.slug), Nil)
  }
