package eu.ww86.digits

enum Sex:
  case Female, Male

/** A first name from the PESEL register: how many living people bear it as the first name, and as the second. */
final case class GivenName(name: String, sex: Sex, asFirst: Int, asSecond: Int):
  /** Second-name bearers for every first-name one. */
  def secondShare: Double = if asFirst == 0 then 0.0 else asSecond.toDouble / asFirst

object GivenName:
  /** Rows of `pesel-names.csv` (see `scripts/pesel-names`): a header, then `name,sex,first,second`, sex F or M. */
  def parse(csv: String): List[GivenName] =
    csv.linesIterator.drop(1).filter(_.nonEmpty).map { line =>
      line.split(',') match
        case Array(name, "F", first, second) => GivenName(name, Sex.Female, first.toInt, second.toInt)
        case Array(name, "M", first, second) => GivenName(name, Sex.Male, first.toInt, second.toInt)
        case _                               => throw IllegalArgumentException(s"not a row of names: $line")
    }.toList

/** Which names the search offers. The page can switch off every filter but the sex. */
final case class NameFilter(sex: Sex, minBearers: Int, alsoSecond: Boolean, polishAlphabet: Boolean):
  def keeps(name: GivenName): Boolean =
    name.sex == sex &&
      name.asFirst >= minBearers &&
      (!alsoSecond || name.secondShare >= NameFilter.minSecondShare) &&
      (!polishAlphabet || !name.name.exists(NameFilter.foreignLetters.contains))

object NameFilter:
  /** Transliterated names are common first names in the register and almost never second ones: OLEKSANDR 0.0006,
    * MAKSYM 0.017. Polish names sit above, LENA 0.096, ANTONI 0.84; TYMON, at 0.048, is the one that falls below.
    */
  val minSecondShare: Double = 0.05

  val foreignLetters: Set[Char] = Set('Q', 'V', 'X')

  val default: NameFilter = NameFilter(Sex.Female, minBearers = 500, alsoSecond = true, polishAlphabet = true)
