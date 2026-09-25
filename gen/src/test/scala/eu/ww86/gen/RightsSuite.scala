package eu.ww86.gen

import eu.ww86.site.{Catalog, Language, Site}
import java.nio.file.{Files, Path}

class RightsSuite extends munit.FunSuite:

  /** What a page `depth` directories below the site root lacks of the notice, its link and the head tags. */
  private def missing(page: String, depth: Int, notice: String): List[String] =
    val terms = "../" * depth + "copyright.html"
    List(
      notice,
      s"""<a href="$terms">""",
      s"""<meta name="author" content="${Site.author}"""",
      """<meta name="tdm-reservation" content="1"""",
      s"""<link rel="license" href="$terms""""
    ).filterNot(page.contains)

  test("every lab page reserves its rights in its own language") {
    // sbt runs the tests in the root of the build, where lab/ is
    val gaps = for
      item <- Catalog.items.filterNot(_.generated)
      gap  <- missing(Files.readString(Path.of(item.path)), 2, Site.rights(item.language, item.year))
    yield s"${item.slug}: $gap"
    assertEquals(gaps, Nil)
  }

  test("every generated page reserves its rights in English") {
    val gaps = for
      (path, page) <- files.filter(_._1.toString.endsWith(".html"))
      gap          <- missing(page, path.getNameCount - 1, Site.rights(Language.English, Site.since))
    yield s"$path: $gap"
    assertEquals(gaps, Nil)
  }

  test("no page names the AI tool it was developed with") {
    val lab   = filesUnder(Path.of("lab"), ".html").map(path => path -> Files.readString(path))
    val pages = files.filter(_._1.toString.endsWith(".html")) ++ lab
    assertEquals(pages.collect { case (path, page) if page.toLowerCase.contains("claude") => path.toString }, Nil)
  }

  test("the terms page every notice links to is generated and gives the legal ground of the reservation") {
    val page = files.toMap.getOrElse(Path.of("copyright.html"), fail("copyright.html is not generated"))
    List("Art. 4(3) of Directive (EU) 2019/790", "Art. 26³", Site.sourceUrl).foreach { text =>
      assert(page.contains(text), s"the terms page does not mention $text")
    }
  }
