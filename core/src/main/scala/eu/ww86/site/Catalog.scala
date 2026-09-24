package eu.ww86.site

/** One entry of the lab.
  *
  * `slug` is the directory under `lab/`: either a self-contained artifact committed there, or a page
  * written by the generator (see `generated`).
  */
final case class LabItem(
    slug: String,
    title: String,
    blurb: String,
    tech: List[String],
    year: Int,
    generated: Boolean = false,
    postUrl: Option[String] = None,
    language: Language = Language.English
):
  /** Path from the site root, so pages can link to it relatively. */
  def path: String = s"lab/$slug/index.html"

  /** The card is always in English; this tells the reader when the page behind it is not. */
  def languageNote: Option[String] =
    Option.when(language != Language.English)(s"In ${language.name}")

/** Language a lab page is written in. `code` goes into `hreflang`. */
enum Language(val code: String, val name: String):
  case English extends Language("en", "English")
  case Polish  extends Language("pl", "Polish")

object Catalog:

  /** Newest first - this order is what the pages show. */
  val items: List[LabItem] = List(
    LabItem(
      slug = "java-streams-scala-3",
      title = "Java Streams and Scala 3 collections",
      blurb = "Fourteen tasks on a toll-road log, each solved with the modern Stream API (teeing, mapMulti, Gatherers) and with Scala 3 collections, side by side with the output. Handy in both directions: reading today's Java with Scala habits, or moving from Java to Scala.",
      tech = List("Java 25", "Scala 3", "drafted with Claude"),
      year = 2026,
      language = Language.Polish
    ),
    LabItem(
      slug = "digits",
      title = "Letter sums and digit roots",
      blurb = "Splits a name into vowels and consonants, adds up their letter values and reduces each sum to a single digit. The arithmetic is a pure Scala module, cross-compiled to the browser.",
      tech = List("Scala 3", "Scala.js", "Laminar"),
      year = 2026,
      generated = true
    ),
    LabItem(
      slug = "ursus-by-train",
      title = "Out of Warsaw by train, from Ursus",
      blurb = "A day out of Warsaw with no car and no change of trains: museums, cinemas, monuments and parks by the stations of three lines from Ursus, two heading west and one east to Otwock, with filters and a walking route from each platform. Timetable details as of September 2026.",
      tech = List("JavaScript", "drafted with Claude"),
      year = 2026,
      language = Language.Polish
    ),
    LabItem(
      slug = "scala-to-java",
      title = "Thinking in Scala, writing Java",
      blurb = "A cheat sheet for Scala developers who have to write Java, in a live-coding interview or someone else's codebase: 35 idioms side by side, 22 traps that compile and still give the wrong answer, and 10 problems solved in both languages.",
      tech = List("Java 21", "Scala", "drafted with Claude"),
      year = 2026,
      language = Language.Polish
    )
  )
