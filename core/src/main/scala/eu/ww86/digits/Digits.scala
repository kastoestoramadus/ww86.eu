package eu.ww86.digits

/** Maps letters to digits and says which of them are vowels.
  *
  * The default table is the classic 1-9 cycle over the Latin alphabet, extended so that every Polish
  * letter with a diacritic gets the digit of its base letter.
  */
final case class LetterTable(values: Map[Char, Int], vowels: Set[Char]):
  def digit(letter: Char): Option[Int] = values.get(letter)
  def isVowel(letter: Char): Boolean   = vowels.contains(letter)
  def covers(word: String): Boolean    = word.forall(values.contains)

object LetterTable:
  /** Latin letters plus Ą Ć Ę Ł Ń Ó Ś Ź Ż, upper case only - normalise input with `Words.normalise`. */
  val polish: LetterTable = LetterTable(
    values = Map(
      'A' -> 1, 'Ą' -> 1, 'B' -> 2, 'C' -> 3, 'Ć' -> 3, 'D' -> 4, 'E' -> 5, 'Ę' -> 5, 'F' -> 6,
      'G' -> 7, 'H' -> 8, 'I' -> 9, 'J' -> 1, 'K' -> 2, 'L' -> 3, 'Ł' -> 3, 'M' -> 4, 'N' -> 5,
      'Ń' -> 5, 'O' -> 6, 'Ó' -> 6, 'P' -> 7, 'Q' -> 8, 'R' -> 9, 'S' -> 1, 'Ś' -> 1, 'T' -> 2,
      'U' -> 3, 'V' -> 4, 'W' -> 5, 'X' -> 6, 'Y' -> 7, 'Z' -> 8, 'Ź' -> 8, 'Ż' -> 8
    ),
    vowels = "AĄEĘIOÓU".toSet
  )

/** Digit sums of a text, split by vowels and consonants. Letters outside the table are ignored. */
final case class Sums(vowels: Int, consonants: Int):
  def total: Int          = vowels + consonants
  def roots: Roots        = Roots(Digits.root(vowels), Digits.root(consonants), Digits.root(total))
  def all: Set[Int]       = Set(vowels, consonants, total)
  def +(other: Sums): Sums = Sums(vowels + other.vowels, consonants + other.consonants)

object Sums:
  val zero: Sums = Sums(0, 0)

/** The three sums of [[Sums]] reduced to single digits. */
final case class Roots(vowels: Int, consonants: Int, total: Int):
  override def toString: String = s"$vowels/$consonants/$total"

object Digits:

  /** Repeated digit sum: 162 -> 1+6+2 = 9. */
  def root(number: Int): Int =
    if number < 10 then number
    else root(number.toString.map(digit => digit - '0').sum)

  def sums(text: String, table: LetterTable = LetterTable.polish): Sums =
    text.foldLeft(Sums.zero) { (acc, letter) =>
      table.digit(letter) match
        case None                             => acc
        case Some(value) if table.isVowel(letter) => acc + Sums(value, 0)
        case Some(value)                      => acc + Sums(0, value)
    }

  /** Sums of a whole name: every word contributes its own letters. */
  def sumsOfWords(words: Seq[String], table: LetterTable = LetterTable.polish): Sums =
    words.foldLeft(Sums.zero)((acc, word) => acc + sums(word, table))

  /** How many times each digit appears in a name. */
  def digitCounts(words: Seq[String], table: LetterTable = LetterTable.polish): Map[Int, Int] =
    words.flatten.flatMap(table.digit).groupMapReduce(identity)(_ => 1)(_ + _)

/** Turns free-form input into the upper-case words the tables expect. */
object Words:
  // Character.isLetter instead of a \p{L} regex: unicode property escapes need ES2018 in Scala.js.
  def normalise(text: String): List[String] =
    text.toUpperCase
      .map(letter => if Character.isLetter(letter) then letter else ' ')
      .split(' ')
      .filter(_.nonEmpty)
      .toList

/** Sums singled out by the caller.
  *
  * This module deliberately ships no labels: what a given sum is supposed to mean belongs to the
  * front-end that asks the question, not to the arithmetic.
  */
final case class Highlights(byLabel: Map[String, Set[Int]]):
  def matching(sums: Sums): Set[String] =
    byLabel.collect { case (label, chosen) if chosen.exists(sums.all.contains) => label }.toSet

object Highlights:
  val none: Highlights = Highlights(Map.empty)

object Candidates:

  /** Groups whole name variants by their triple of digit roots. */
  def byRoots(
      variants: Seq[Seq[String]],
      table: LetterTable = LetterTable.polish
  ): Map[Roots, Seq[Seq[String]]] =
    variants.groupBy(words => Digits.sumsOfWords(words, table).roots)

  /** Words from `pool` that are covered by the table and hit at least one highlighted sum. */
  def matching(
      pool: Seq[String],
      highlights: Highlights,
      table: LetterTable = LetterTable.polish
  ): Seq[String] =
    pool.filter(word => table.covers(word) && highlights.matching(Digits.sums(word, table)).nonEmpty)
