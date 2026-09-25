package eu.ww86.site

/** Texts and links shown on every page. Keep them in sync with the blog's `_config.yml` and about page. */
object Site:
  val domain      = "ww86.eu"
  val author      = "Waldemar Wosiński"
  val tagline     = "Scala & Big Data engineer from Warsaw."
  val description = "Things I build: small tools, demos and experiments by Waldemar Wosiński, a Scala & Big Data engineer from Warsaw."

  val about: List[String] = List(
    "I am a Scala & Big Data engineer from Warsaw with 15+ years of commercial experience in banking, fintech and the public sector.",
    "This is where the things I build end up: small tools, demos and experiments, each one a page you can open and use. The writing lives on the blog."
  )

  val blogUrl     = "https://blog.ww86.eu"
  val githubUrl   = "https://github.com/kastoestoramadus"
  val linkedInUrl = "https://www.linkedin.com/in/waldemar-wosi%C5%84ski-a9936144/"
  val contactUrl  = s"$blogUrl/#contact"
  val sourceUrl   = s"$githubUrl/ww86.eu"

  /** Year the site was first published, the year in the notice of the generated pages. */
  val since = 2026

  /** Rights notice at the foot of a page. A lab page holds it verbatim, `RightsSuite` in `gen` checks that. */
  def rights(language: Language, year: Int): String = language match
    case Language.English =>
      s"© $year $author. All rights reserved, including text and data mining (Art. 4(3) of Directive (EU) 2019/790). Developed with agentic AI systems."
    case Language.Polish =>
      s"© $year $author. Wszelkie prawa zastrzeżone, również w zakresie eksploracji tekstów i danych (art. 26³ ustawy o prawie autorskim i prawach pokrewnych). Strona rozwijana z użyciem systemów agentowych."
