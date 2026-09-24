package eu.ww86.web

import com.raquo.laminar.api.L.{*, given}
import eu.ww86.digits.{Digits, Sums, Words}

object DigitsWidget:

  def apply(): HtmlElement =
    val entered = Var("")
    val words   = entered.signal.map(Words.normalise)

    div(
      cls := "widget",
      label(
        cls := "field",
        span("Name or word"),
        input(
          tpe         := "text",
          placeholder := "Ada Lovelace",
          value <-- entered,
          onInput.mapToValue --> entered
        )
      ),
      child <-- words.map {
        case Nil  => p(cls := "muted", "Type something to see its sums.")
        case list => results(list)
      }
    )

  private def results(list: List[String]): HtmlElement =
    val whole = Digits.sumsOfWords(list)
    div(
      table(
        cls := "sums",
        thead(tr(th("word"), th("vowels"), th("consonants"), th("total"), th("roots"))),
        tbody(
          list.map(word => row(word, Digits.sums(word))),
          if list.sizeIs > 1 then row(list.mkString(" "), whole, isTotal = true) else emptyNode
        )
      ),
      p(
        cls := "summary",
        "Digit roots: ",
        strong(whole.roots.toString),
        s" from ${whole.vowels} + ${whole.consonants} = ${whole.total}."
      ),
      table(
        cls := "sums counts",
        thead(tr(th("letters per digit"), (1 to 9).map(digit => th(digit.toString)))),
        tbody(
          list.map(word => countsRow(word, Digits.countsPerDigit(List(word)))),
          if list.sizeIs > 1 then countsRow(list.mkString(" "), Digits.countsPerDigit(list), isTotal = true)
          else emptyNode
        )
      )
    )

  private def row(word: String, sums: Sums, isTotal: Boolean = false): HtmlElement =
    tr(
      cls("total") := isTotal,
      th(word),
      td(sums.vowels.toString),
      td(sums.consonants.toString),
      td(sums.total.toString),
      td(sums.roots.toString)
    )

  private def countsRow(word: String, counts: List[(Int, Int)], isTotal: Boolean = false): HtmlElement =
    tr(
      cls("total") := isTotal,
      th(word),
      counts.map((_, count) => td(cls("zero") := count == 0, count.toString))
    )
