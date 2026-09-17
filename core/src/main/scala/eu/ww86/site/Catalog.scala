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
    postUrl: Option[String] = None
):
  def url: String = s"/lab/$slug/"

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
