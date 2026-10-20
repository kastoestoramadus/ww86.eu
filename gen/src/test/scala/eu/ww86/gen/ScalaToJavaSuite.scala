package eu.ww86.gen

import java.nio.file.{Files, Path}

/** The scala-to-java page is copied verbatim, so its own layout conventions hold as tests.
  * Read from the repository root, the way `UrsusSuite` reads `lab/` pages.
  */
class ScalaToJavaSuite extends munit.FunSuite:

  private val page = Files.readString(Path.of("lab", "scala-to-java", "index.html"))

  /** The declarations of one exact selector inside a media block, found by what it styles. */
  private def mediaRule(media: String, selector: String): Option[String] =
    val pattern = ("""(?s)\Q""" + media + """\E\s*\{((?:[^{}]*\{[^{}]*\})*[^{}]*)\}""").r
    pattern
      .findAllMatchIn(page)
      .map(_.group(1))
      .collectFirst { case block if block.contains(selector + "{") =>
        block.substring(block.indexOf(selector + "{") + selector.length + 1).takeWhile(_ != '}')
      }

  test("the phone-width traps grid can shrink below its content, so the page does not scroll sideways") {
    // A `1fr` track has an auto minimum: one wide code sample sizes the shared track for every
    // trap and the document overflows horizontally below ~590px. `minmax(0,1fr)` lets the track
    // shrink; the code samples scroll inside their own wrappers, as the pairs already do.
    val traps = mediaRule("@media (max-width:820px)", ".traps")
    assertEquals(traps, Some("grid-template-columns:minmax(0,1fr)"))
  }
