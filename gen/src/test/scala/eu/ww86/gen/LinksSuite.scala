package eu.ww86.gen

class LinksSuite extends munit.FunSuite:

  test("links and assets from the site root are found") {
    assertEquals(
      Links.fromSiteRoot("""<a href="/lab/">x</a><img src='/me.png'><link rel=stylesheet href=/css/a.css>"""),
      List("/lab/", "/me.png", "/css/a.css")
    )
  }

  test("relative, protocol-relative, absolute and in-page links are fine") {
    val page =
      """<a href="../index.html">a</a><a href="index.html#about">b</a><a href="#top">c</a>
        |<a href="//cdn.example.org/x.js">d</a><a href="https://ww86.eu/">e</a><a href="mailto:x@example.org">f</a>""".stripMargin
    assertEquals(Links.fromSiteRoot(page), Nil)
  }

  test("every srcset candidate is checked") {
    assertEquals(Links.fromSiteRoot("""<img srcset="a.png 1x, /b.png 2x" src="a.png">"""), List("/b.png"))
  }

  test("css url() from the site root is found, quoted or not") {
    val css = """body { background: url(/img/bg.png) } .a { background: url("../a.png") } @font-face { src: url('/f.woff2') }"""
    assertEquals(Links.fromSiteRoot(css), List("/img/bg.png", "/f.woff2"))
  }

  test("code samples in the text are not links") {
    val page = """<pre>&lt;a href="/x"&gt; Source.fromURL("/y")</pre>"""
    assertEquals(Links.fromSiteRoot(page), Nil)
  }

  test("the generated pages link nothing from the site root") {
    List(Pages.landing, Pages.labIndex, Pages.digits).foreach { page =>
      assertEquals(Links.fromSiteRoot(page), Nil)
    }
  }
