package eu.ww86.gen

class PreviewSuite extends munit.FunSuite:

  private val robots = """<meta name="robots" content="noindex, nofollow">"""

  test("the robots meta goes right after <head>") {
    assertEquals(
      Preview.noindex("<html><head><title>x</title></head><body></body></html>"),
      Right(s"<html><head>$robots<title>x</title></head><body></body></html>")
    )
  }

  test("attributes and case of <head> are kept") {
    assertEquals(Preview.noindex("""<HEAD lang="pl"><title>x</title>"""), Right(s"""<HEAD lang="pl">$robots<title>x</title>"""))
  }

  test("a page without <head> is refused, <header> is not a head") {
    assert(Preview.noindex("<body><header>x</header></body>").isLeft)
  }

  test("every generated page can be marked, once") {
    List(Pages.landing, Pages.labIndex, Pages.digits).foreach { page =>
      val marked = Preview.noindex(page).toOption.get
      assertEquals(marked.sliding(robots.length).count(_ == robots), 1)
    }
  }
