package eu.ww86.web

import com.raquo.laminar.api.L.{*, given}
import eu.ww86.digits.{Candidates, Digits, GivenName, NameFilter, Roots, Sex, SpecialSums}
import scala.util.{Failure, Success, Try}

/** Looks through the PESEL first names for one that, added to the typed name, gives it the wanted roots. */
object NameSearch:

  private val shown       = 100
  private val bearerSteps = List(20, 100, 500, 1000, 10000)

  def apply(words: Signal[List[String]]): HtmlElement =
    val wanted = Var(Option.empty[Roots])
    val filter = Var(NameFilter.default)
    // written next to the page by the generator, fetched once when the widget mounts
    val names: Signal[Option[Try[List[GivenName]]]] =
      FetchStream
        .get("names.csv")
        .recoverToTry
        .map(text => Some(text.flatMap(csv => Try(GivenName.parse(csv)))))
        .startWith(None)

    div(
      cls := "search",
      h3("Add one name"),
      label(
        cls := "field",
        span("Wanted roots of the whole name: vowels/consonants/total"),
        select(
          option(value := "", s"choose one of the ${Roots.possible.size} a name can have"),
          Roots.possible.map(roots => option(value := roots.toString, roots.toString)),
          value <-- wanted.signal.map(_.fold("")(_.toString)),
          onChange.mapToValue --> (text => wanted.set(Roots.parse(text)))
        )
      ),
      filters(filter),
      child <-- words.combineWith(wanted.signal, filter.signal, names).map(outcome)
    )

  private def filters(filter: Var[NameFilter]): HtmlElement =
    div(
      cls := "filters",
      Sex.values.toList.map { sex =>
        label(
          input(
            tpe      := "radio",
            nameAttr := "search-sex",
            checked <-- filter.signal.map(_.sex == sex),
            onClick --> (_ => filter.update(_.copy(sex = sex)))
          ),
          if sex == Sex.Female then " women's names" else " men's names"
        )
      },
      label(
        "borne first by at least ",
        select(
          bearerSteps.map(step => option(value := step.toString, grouped(step))),
          value <-- filter.signal.map(_.minBearers.toString),
          onChange.mapToValue --> (step => filter.update(_.copy(minBearers = step.toInt)))
        ),
        " people"
      ),
      label(
        input(
          tpe := "checkbox",
          checked <-- filter.signal.map(_.alsoSecond),
          onClick.mapToChecked --> (on => filter.update(_.copy(alsoSecond = on)))
        ),
        " also a second name, to 1 in 20 of those who bear it first"
      ),
      label(
        input(
          tpe := "checkbox",
          checked <-- filter.signal.map(_.polishAlphabet),
          onClick.mapToChecked --> (on => filter.update(_.copy(polishAlphabet = on)))
        ),
        " Polish alphabet: no Q, V or X"
      )
    )

  private def outcome(
      words: List[String],
      wanted: Option[Roots],
      filter: NameFilter,
      names: Option[Try[List[GivenName]]]
  ): HtmlElement =
    (wanted, names) match
      case (None, _)                          => p(cls := "muted", "Choose the roots the whole name should have.")
      case (_, None)                          => p(cls := "muted", "Loading the names…")
      case (_, Some(Failure(_)))              => p(cls := "muted", "The names could not be loaded.")
      case (Some(wanted), Some(Success(all))) => results(words, wanted, all.filter(filter.keeps))

  private def results(words: List[String], wanted: Roots, pool: List[GivenName]): HtmlElement =
    val base  = Digits.sumsOfWords(words)
    val found = Candidates.completing(words, wanted, pool)
    val whole = if words.isEmpty then "have" else s"give ${words.mkString(" ")}"
    div(
      p(cls := "summary", s"${grouped(found.size)} of ${grouped(pool.size)} names $whole the roots $wanted."),
      if found.isEmpty then emptyNode
      else
        table(
          cls := "sums found",
          thead(tr(th("name"), th("people"), th("sums of the whole name"))),
          tbody(found.take(shown).map { name =>
            val sums = base + Digits.sums(name.name)
            tr(
              th(name.name),
              td(grouped(name.asFirst)),
              td(
                List(sums.vowels, sums.consonants, sums.total)
                  .map(sum => span(cls := "sum", cls("special") := SpecialSums.isSpecial(sum), sum.toString))
                  .flatMap(sum => List(sum, span(" / ")))
                  .init
              )
            )
          })
        ),
      if found.sizeIs > shown then p(cls := "muted", s"The ${grouped(found.size - shown)} less common ones are left out.")
      else emptyNode
    )

  /** 10000 as 10,000, the same on the JVM and in the browser. */
  private def grouped(number: Int): String = number.toString.reverse.grouped(3).mkString(",").reverse
