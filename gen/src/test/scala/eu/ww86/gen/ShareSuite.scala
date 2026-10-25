package eu.ww86.gen

import eu.ww86.site.Site
import java.nio.file.{Files, Path}

/** Sharing is part of the page: what a link preview shows is pinned like any other markup. */
class ShareSuite extends munit.FunSuite:

  private val pages = files.filter(_._1.toString.endsWith(".html"))

  /** The content attribute of one meta element, so a tag is tested by what it says. */
  private def meta(page: String, key: String, attribute: String = "property"): String =
    s"""(?s)<meta $attribute="$key" content="([^"]*)"""".r
      .findFirstMatchIn(page).map(_.group(1)).getOrElse(fail(s"no meta $attribute=$key"))

  test("every generated page carries the Open Graph set with the page's own title and description") {
    val canonical = Map(
      "index.html"             -> "https://ww86.eu/",
      "copyright.html"         -> "https://ww86.eu/copyright.html",
      "lab/index.html"         -> "https://ww86.eu/lab/",
      "lab/digits/index.html"  -> "https://ww86.eu/lab/digits/"
    )
    pages.foreach { (path, page) =>
      val url = canonical.getOrElse(path.toString, fail(s"unexpected page $path"))
      val title =
        """<title>([^<]*)</title>""".r.findFirstMatchIn(page).map(_.group(1)).getOrElse(fail("no title"))
      assertEquals(meta(page, "og:title"), title, path.toString)
      assertEquals(meta(page, "og:description"), meta(page, "description", "name"), path.toString)
      assertEquals(meta(page, "og:type"), "website", path.toString)
      assertEquals(meta(page, "og:site_name"), Site.domain, path.toString)
      assertEquals(meta(page, "og:url"), url, path.toString)
    }
  }

  test("the share image is one real PNG of the size the meta promises, with an SVG source beside it") {
    pages.foreach { (path, page) =>
      assertEquals(meta(page, "og:image"), "https://ww86.eu/share.png", path.toString)
      assertEquals(meta(page, "og:image:width"), "1200", path.toString)
      assertEquals(meta(page, "og:image:height"), "630", path.toString)
      assert(meta(page, "og:image:alt").contains(Site.author), path.toString)
    }

    val png = Files.readAllBytes(Path.of("static", "share.png")).map(b => b & 0xff).toList
    assertEquals(png.take(8), List(0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a), "not a PNG")
    assertEquals(png.slice(12, 16), List(0x49, 0x48, 0x44, 0x52), "no IHDR where the format puts it")
    assertEquals(bigEndian(png.slice(16, 20)), 1200, "width")
    assertEquals(bigEndian(png.slice(20, 24)), 630, "height")
    assert(png.length > 1000 && png.length < 400000, s"share.png is ${png.length} bytes")

    val svg = Files.readString(Path.of("static", "share.svg"))
    List("""width="1200"""", """height="630"""", Site.author, Site.tagline.replace("&", "&amp;"))
      .foreach(text => assert(svg.contains(text), s"share.svg does not carry $text"))
  }

  test("the Twitter summary card matches the Open Graph image") {
    pages.foreach { (path, page) =>
      assertEquals(meta(page, "twitter:card", "name"), "summary_large_image", path.toString)
    }
  }

  private def bigEndian(bytes: List[Int]): Int = bytes.foldLeft(0)((sum, b) => (sum << 8) | b)
