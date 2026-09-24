package eu.ww86.digits

class DigitsSuite extends munit.FunSuite:

  test("digit root collapses to a single digit") {
    assertEquals(Digits.root(9), 9)
    assertEquals(Digits.root(32), 5)
    assertEquals(Digits.root(41), 5)
  }

  test("sums split vowels from consonants") {
    assertEquals(Digits.sums("SCALA"), Sums(vowels = 2, consonants = 7))
    assertEquals(Digits.sums("SCALA").total, 9)
    assertEquals(Digits.sums("SCALA").roots, Roots(2, 7, 9))
  }

  test("a name is the sum of its words") {
    val words = List("SCALA", "LAMINAR")
    assertEquals(Digits.sumsOfWords(words), Sums(vowels = 13, consonants = 28))
    assertEquals(Digits.sumsOfWords(words).roots, Roots(4, 1, 5))
  }

  test("diacritics count as their base letter") {
    assertEquals(Digits.sums("ŁÓDŹ"), Digits.sums("LODZ"))
    assertEquals(Digits.sums("ŻÓŁW"), Sums(vowels = 6, consonants = 16))
  }

  test("letters outside the table are ignored") {
    assertEquals(Digits.sums("SCALA 3!"), Digits.sums("SCALA"))
  }

  // Scala.js and the JVM disagree on locale-sensitive upper casing, so this runs on both platforms.
  test("normalise upper cases and splits on anything that is not a letter") {
    assertEquals(Words.normalise("scala laminar"), List("SCALA", "LAMINAR"))
    assertEquals(Words.normalise("  jvm-js  "), List("JVM", "JS"))
    assertEquals(Words.normalise("łódź"), List("ŁÓDŹ"))
  }

  test("digit counts add up to the number of letters") {
    val counts = Digits.digitCounts(List("SCALA"))
    assertEquals(counts.values.sum, 5)
    assertEquals(counts.get(1), Some(3))
    assertEquals(counts.get(3), Some(2))
  }

  test("counts per digit list all nine digits in order, zeros included") {
    assertEquals(
      Digits.countsPerDigit(List("SCALA")),
      List(1 -> 3, 2 -> 0, 3 -> 2, 4 -> 0, 5 -> 0, 6 -> 0, 7 -> 0, 8 -> 0, 9 -> 0)
    )
    assertEquals(Digits.countsPerDigit(Nil).map(_._2), List.fill(9)(0))
  }

  test("special sums are looked for among the vowel, consonant and total sums, each value once") {
    assertEquals(SpecialSums.in(Sums(vowels = 2, consonants = 7)), Nil)
    assertEquals(SpecialSums.in(Sums(vowels = 13, consonants = 9)), List(13, 22))
    assertEquals(SpecialSums.in(Sums(vowels = 11, consonants = 11)), List(11, 22))
    assertEquals(SpecialSums.in(Sums(vowels = 4, consonants = 23)), List(27))
  }

  test("special sums reach past two digits only with 111 and 222") {
    assertEquals(List(111, 222, 333, 12, 0).map(SpecialSums.isSpecial), List(true, true, false, false, false))
  }

  test("a sum is special on its raw value, not on its digit root") {
    // 29 and 38 both reduce through 11
    assertEquals(SpecialSums.in(Sums(vowels = 20, consonants = 9)), Nil)
    assertEquals(SpecialSums.in(Sums(vowels = 20, consonants = 18)), Nil)
  }

  test("any one of the three sums is enough") {
    assertEquals(SpecialSums.in(Sums(vowels = 17, consonants = 3)), List(17))
    assertEquals(SpecialSums.in(Sums(vowels = 2, consonants = 41)), List(41))
    assertEquals(SpecialSums.in(Sums(vowels = 5, consonants = 6)), List(11))
  }

  test("a special total can repeat a special part and still counts once") {
    assertEquals(SpecialSums.in(Sums(vowels = 0, consonants = 222)), List(222))
    assertEquals(SpecialSums.in(Sums(vowels = 100, consonants = 11)), List(11, 111))
  }

  test("a name with no letters from the table has no special sum") {
    assertEquals(SpecialSums.in(Digits.sumsOfWords(Words.normalise(" - 3 "))), Nil)
  }

  test("highlights report every label a word hits") {
    val highlights = Highlights(Map("a" -> Set(9), "b" -> Set(2), "c" -> Set(99)))
    assertEquals(highlights.matching(Digits.sums("SCALA")), Set("a", "b"))
    assertEquals(Highlights.none.matching(Digits.sums("SCALA")), Set.empty[String])
  }

  test("candidates keep only words hitting a highlight") {
    val pool = List("SCALA", "LAMINAR", "ŻÓŁW")
    assertEquals(Candidates.matching(pool, Highlights(Map("x" -> Set(11)))), List("LAMINAR"))
  }

  test("variants group by their roots") {
    val grouped = Candidates.byRoots(List(List("SCALA"), List("LAMINAR")))
    assertEquals(grouped.keySet, Set(Roots(2, 7, 9), Roots(2, 3, 5)))
  }
