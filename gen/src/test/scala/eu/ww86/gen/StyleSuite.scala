package eu.ww86.gen

import java.nio.file.{Files, Path}

/** The stylesheet is source like any other, so the shared conventions of the site hold as tests.
  * Read from the repository root, the way `UrsusSuite` reads `lab/` pages.
  */
class StyleSuite extends munit.FunSuite:

  private val css = Files.readString(Path.of("static", "css", "site.css"))

  /** The declarations of one exact selector, so a rule is found by what it styles, not by order. */
  private def rule(selector: String): Option[String] =
    val block = ("(?s)\\Q" + selector + "\\E\\s*\\{([^}]*)\\}").r
    block.findFirstMatchIn(css).map(_.group(1))

  test("the dark colour scheme is declared, so UA controls and canvases stay dark") {
    assert(rule(":root").exists(_.contains("color-scheme: dark")))
  }

  test("font sizes come only from the five type tokens") {
    List("--text-xs", "--text-s", "--text-m", "--text-l", "--text-xl").foreach { token =>
      assert(css.contains(s"$token:"), s"missing token $token")
    }
    val adHoc = """font-size:\s*([^;}]+)""".r.findAllMatchIn(css).map(_.group(1).trim).toList
      .filterNot(_.startsWith("var(--text-"))
    assertEquals(adHoc, Nil, s"ad-hoc font sizes: $adHoc")
  }

  test("spacing is tokenised on a 4px base and carries the section rhythm") {
    List("--s1", "--s2", "--s3", "--s4", "--s5", "--s6").foreach { token =>
      assert(css.contains(s"$token:"), s"missing token $token")
    }
    assert(rule("h2").exists(_.contains("margin: var(--s6)")), "h2 no longer opens a section")
  }

  test("the measure is one 42rem spine, wide enough for cards and narrow enough for reading") {
    assert(rule(":root").exists(_.contains("--max: 42rem")))
  }

  test("keyboard focus is one visible pattern, on the accent colour") {
    val focus = rule("a:focus-visible, .card:focus-visible")
    assert(focus.exists(_.contains("outline: 2px solid var(--accent)")))
    assert(focus.exists(_.contains("outline-offset: 2px")))
  }

  test("links in running text are underlined") {
    assert(
      rule("main p a, .breadcrumb a, footer a, .about .links a").exists(_.contains("text-decoration: underline"))
    )
  }

  test("both sums tables wrap a typed name, so a 360px phone needs no sideways scroll") {
    assert(rule("table.sums tbody th").exists(_.contains("overflow-wrap: anywhere")))
    assert(rule("table.counts tbody th").exists(_.contains("overflow-wrap: anywhere")))
    assert(rule(".widget").exists(_.contains("overflow-x: auto")), "a table wider than the phone must scroll inside the widget, not the page")
  }

  test("interactive control boundaries use the strong border colour") {
    assert(css.contains("--border-strong:"))
    assert(rule(".field input, .field select").exists(_.contains("var(--border-strong)")))
  }
