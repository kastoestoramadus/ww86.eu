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

  /** The card is always in English, so its flag says what language the page behind it is in. */
  def languageLabel: String = s"In ${language.name}"

/** Language a lab page is written in. `code` goes into `hreflang`, `flag` names `static/flags/<flag>.svg`. */
enum Language(val code: String, val name: String, val flag: String):
  case English extends Language("en", "English", "gb")
  case Polish  extends Language("pl", "Polish", "pl")

object Catalog:

  /** Newest first - this order is what the pages show. */
  val items: List[LabItem] = List(
    LabItem(
      slug = "scala-to-java",
      title = "Thinking in Scala, writing Java",
      blurb = "A cheat sheet for Scala developers who have to write Java, in a live-coding interview or someone else's codebase: 35 idioms side by side, 10 problems and 14 stream tasks solved in both languages with the output, and 22 traps that compile and still give the wrong answer. Handy in both directions: reading today's Java with Scala habits, or moving from Java to Scala.",
      tech = List("Java 25", "Scala 3"),
      year = 2026,
      language = Language.Polish
    ),
    LabItem(
      slug = "digits",
      title = "Letter sums and digit roots",
      blurb = "Splits a name into vowels and consonants, adds up their letter values, reduces each sum to a single digit and counts how many letters carry each digit. It can also find a first name from the PESEL register that gives a name the wanted roots. The arithmetic is a pure Scala module, cross-compiled to the browser.",
      tech = List("Scala 3", "Scala.js", "Laminar"),
      year = 2026,
      generated = true
    ),
    LabItem(
      slug = "ursus-by-train",
      title = "Out of Warsaw by train, from Ursus",
      blurb = "A day out with no car and no change, planned from Szamoty in Ursus: museums, cinemas, monuments, parks and shops by the stations of three train lines, to Skierniewice, Łowicz and Otwock, and four bus lines, with rarer trains apart and ticket zones marked. Filters and a walking route from each stop. Timetables as of September 2026.",
      tech = List("JavaScript"),
      year = 2026,
      language = Language.Polish
    )
  )
