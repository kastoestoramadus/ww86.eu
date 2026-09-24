package eu.ww86.gen

import java.nio.file.{Files, Path}

/** The site is served at https://ww86.eu/ and, as a PR preview, below `/preview/pr-N/`. A link from the
  * site root (`/lab/`) works in the first place only: in a preview it silently leads to production.
  */
object Links:

  // Attributes only inside real tags: markup shown as text is escaped (`&lt;a href=...`) and never matches.
  private val tag       = """<[A-Za-z][^>]*>""".r
  private val attribute = """(?i)\b(href|src|action|poster|srcset)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'>]+))""".r
  private val cssUrl    = """(?i)\burl\(\s*(?:"([^"]*)"|'([^']*)'|([^\s"')]+))""".r

  /** Links in an HTML page or a stylesheet that start at the site root, in order of appearance. */
  def fromSiteRoot(text: String): List[String] =
    val inTags = for
      t    <- tag.findAllIn(text).toList
      attr <- attribute.findAllMatchIn(t).toList
      url  <- if attr.group(1).equalsIgnoreCase("srcset") then srcsetUrls(value(attr)) else List(value(attr))
    yield url
    val inCss = cssUrl.findAllMatchIn(text).map(value).toList
    (inTags ++ inCss).filter(url => url.startsWith("/") && !url.startsWith("//"))

  private def value(m: scala.util.matching.Regex.Match): String =
    m.subgroups.takeRight(3).find(_ != null).getOrElse("").trim

  private def srcsetUrls(srcset: String): List[String] =
    srcset.split(',').toList.map(_.trim.takeWhile(!_.isWhitespace)).filter(_.nonEmpty)

/** Fails the build when a page or stylesheet of the assembled site links from the site root. */
@main def checkLinks(target: String): Unit =
  val root  = Path.of(target)
  val files = filesUnder(root, ".html", ".css")
  val problems = files.flatMap { file =>
    Links.fromSiteRoot(Files.readString(file)).map(url => s"  ${root.relativize(file)}: $url")
  }
  if problems.nonEmpty then
    throw IllegalStateException(
      problems.mkString(s"${problems.size} link(s) from the site root, they would break a preview:\n", "\n", "")
    )
  println(s"checked ${files.size} pages and stylesheets in $root: all links relative")
