package eu.ww86.digits

/** Values produced by the Scala 2 scratchpad this module was rewritten from, run unchanged.
  *
  * The rewrite gave the same sums, roots and digit counts on every name of the PESEL first and second
  * name lists (January 2026) and on 300 000 random combinations of two to five of them. These rows keep
  * a sample of that comparison: every letter of the table appears at least once.
  */
class ReferenceSuite extends munit.FunSuite:

  // name, vowel sum, consonant sum, roots, digit counts
  private val reference = List(
    ("ANNA", 2, 10, Roots(2, 1, 3), Map(1 -> 2, 5 -> 2)),
    ("PIOTR", 15, 18, Roots(6, 9, 6), Map(2 -> 1, 6 -> 1, 7 -> 1, 9 -> 2)),
    ("KRZYSZTOF", 6, 43, Roots(6, 7, 4), Map(1 -> 1, 2 -> 2, 6 -> 2, 7 -> 1, 8 -> 2, 9 -> 1)),
    ("MAŁGORZATA", 9, 33, Roots(9, 6, 6), Map(1 -> 3, 2 -> 1, 3 -> 1, 4 -> 1, 6 -> 1, 7 -> 1, 8 -> 1, 9 -> 1)),
    ("MICHAŁ", 10, 18, Roots(1, 9, 1), Map(1 -> 1, 3 -> 2, 4 -> 1, 8 -> 1, 9 -> 1)),
    ("MAGDALENA", 8, 23, Roots(8, 5, 4), Map(1 -> 3, 3 -> 1, 4 -> 2, 5 -> 2, 7 -> 1)),
    ("ELŻBIETA", 20, 15, Roots(2, 6, 8), Map(1 -> 1, 2 -> 2, 3 -> 1, 5 -> 2, 8 -> 1, 9 -> 1)),
    ("JĘDRZEJ", 10, 23, Roots(1, 5, 6), Map(1 -> 2, 4 -> 1, 5 -> 2, 8 -> 1, 9 -> 1)),
    ("JAŚMINA", 11, 11, Roots(2, 2, 4), Map(1 -> 4, 4 -> 1, 5 -> 1, 9 -> 1)),
    ("DĄBRÓWKA", 8, 22, Roots(8, 4, 3), Map(1 -> 2, 2 -> 2, 4 -> 1, 5 -> 1, 6 -> 1, 9 -> 1)),
    ("WIEŃCZYSŁAW", 15, 37, Roots(6, 1, 7), Map(1 -> 2, 3 -> 2, 5 -> 4, 7 -> 1, 8 -> 1, 9 -> 1)),
    ("KUŹMA", 4, 14, Roots(4, 5, 9), Map(1 -> 1, 2 -> 1, 3 -> 1, 4 -> 1, 8 -> 1)),
    ("SREĆKO", 11, 15, Roots(2, 6, 8), Map(1 -> 1, 2 -> 1, 3 -> 1, 5 -> 1, 6 -> 1, 9 -> 1)),
    ("JACQUELINE", 23, 20, Roots(5, 2, 7), Map(1 -> 2, 3 -> 3, 5 -> 3, 8 -> 1, 9 -> 1)),
    ("VIOLETTA", 21, 11, Roots(3, 2, 5), Map(1 -> 1, 2 -> 2, 3 -> 1, 4 -> 1, 5 -> 1, 6 -> 1, 9 -> 1)),
    ("XAWERY", 6, 27, Roots(6, 9, 6), Map(1 -> 1, 5 -> 2, 6 -> 1, 7 -> 1, 9 -> 1)),
    ("MARIA JÓZEFA", 23, 28, Roots(5, 1, 6), Map(1 -> 4, 4 -> 1, 5 -> 1, 6 -> 2, 8 -> 1, 9 -> 2)),
    ("JAN KRZYSZTOF", 7, 49, Roots(7, 4, 2), Map(1 -> 3, 2 -> 2, 5 -> 1, 6 -> 2, 7 -> 1, 8 -> 2, 9 -> 1)),
    ("ZOFIA ANNA MARIA", 29, 37, Roots(2, 1, 3), Map(1 -> 5, 4 -> 1, 5 -> 2, 6 -> 2, 8 -> 1, 9 -> 3))
  )

  reference.foreach { (name, vowels, consonants, roots, counts) =>
    test(s"$name as the reference computes it") {
      val words = Words.normalise(name)
      assertEquals(Digits.sumsOfWords(words), Sums(vowels, consonants))
      assertEquals(Digits.sumsOfWords(words).roots, roots)
      assertEquals(Digits.digitCounts(words), counts)
    }
  }

  test("every letter of the table is covered") {
    val covered = reference.flatMap((name, _, _, _, _) => name.filterNot(_ == ' ')).toSet
    assertEquals(LetterTable.polish.values.keySet -- covered, Set.empty[Char])
  }

  test("an empty name has zero roots, as in the reference") {
    assertEquals(Digits.sumsOfWords(Nil).roots, Roots(0, 0, 0))
    assertEquals(Digits.digitCounts(Nil), Map.empty[Int, Int])
  }

  test("names added to the same word group by roots as in the reference") {
    val pool    = List("ANNA", "PIOTR", "MARIA", "JAN", "ZOFIA", "ADAM", "EWA", "TOMASZ")
    val grouped = Candidates.byRoots(pool.map(name => List(name, "LAMINAR")))
    assertEquals(
      grouped.map((roots, variants) => roots -> variants.map(_.head)),
      Map(
        Roots(3, 9, 3) -> List("JAN"),
        Roots(4, 2, 6) -> List("ADAM"),
        Roots(4, 4, 8) -> List("ANNA"),
        Roots(4, 7, 2) -> List("MARIA"),
        Roots(8, 3, 2) -> List("PIOTR"),
        Roots(8, 8, 7) -> List("EWA"),
        Roots(9, 8, 8) -> List("ZOFIA"),
        Roots(9, 9, 9) -> List("TOMASZ")
      )
    )
  }
