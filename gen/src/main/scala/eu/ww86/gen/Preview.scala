package eu.ww86.gen

import java.nio.file.{Files, Path}

/** A PR preview is the built site copied below `/preview/pr-N/` of the same domain; this keeps it out of
  * search results. The relative links need no rewriting, see `Links`.
  */
object Preview:

  private val robots  = """<meta name="robots" content="noindex, nofollow">"""
  private val headTag = """(?i)<head(\s[^>]*)?>""".r

  /** `html` with the robots meta right after its `<head>`, or why it could not be put there. */
  def noindex(html: String): Either[String, String] =
    headTag.findFirstMatchIn(html) match
      case Some(head) => Right(html.patch(head.end, robots, 0))
      case None       => Left("no <head> to put the robots meta in")

/** Marks every page of a copy of the site as a preview; fails on a page it cannot mark. */
@main def markPreview(target: String): Unit =
  val root  = Path.of(target)
  val pages = filesUnder(root, ".html")
  pages.foreach { page =>
    Preview.noindex(Files.readString(page)) match
      case Right(marked) => Files.writeString(page, marked)
      case Left(problem) => throw IllegalStateException(s"${root.relativize(page)}: $problem")
  }
  println(s"marked ${pages.size} pages noindex in $root")
