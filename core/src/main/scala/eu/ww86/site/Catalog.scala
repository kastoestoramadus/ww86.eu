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
      slug = "digits",
      title = "Letter sums and digit roots",
      blurb = "Splits a name into vowels and consonants, adds up their letter values and reduces each sum to a single digit. The arithmetic is a pure Scala module, cross-compiled to the browser.",
      tech = List("Scala 3", "Scala.js", "Laminar"),
      year = 2026,
      generated = true
    )
  )

  def featured: List[LabItem] = items.take(3)
