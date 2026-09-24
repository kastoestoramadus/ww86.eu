package eu.ww86.digits

class NamesSuite extends munit.FunSuite:

  // counts from the PESEL register as of 2026-01-20
  private val hanna     = GivenName("HANNA", Sex.Female, 231647, 36833)
  private val violetta  = GivenName("VIOLETTA", Sex.Female, 20692, 4880)
  private val oleksandr = GivenName("OLEKSANDR", Sex.Male, 125101, 69)
  private val tymon     = GivenName("TYMON", Sex.Male, 43886, 2124)
  private val xawery    = GivenName("XAWERY", Sex.Male, 411, 159)

  private val everything = NameFilter(Sex.Female, minBearers = 0, alsoSecond = false, polishAlphabet = false)

  test("names are read from the rows of the data file") {
    assertEquals(
      GivenName.parse("name,sex,first,second\nHANNA,F,231647,36833\nTYMON,M,43886,2124\n"),
      List(hanna, tymon)
    )
  }

  test("a row with an unknown sex is refused") {
    intercept[IllegalArgumentException](GivenName.parse("name,sex,first,second\nHANNA,K,231647,36833\n"))
  }

  test("the second-name share sets second-name bearers against first-name ones") {
    assertEquals(GivenName("X", Sex.Male, 200, 10).secondShare, 0.05)
    assertEquals(GivenName("X", Sex.Male, 0, 10).secondShare, 0.0)
  }

  test("the filter keeps only names of the chosen sex") {
    assert(everything.keeps(hanna))
    assert(!everything.keeps(tymon))
    assert(everything.copy(sex = Sex.Male).keeps(tymon))
  }

  test("the filter keeps names borne first by at least the minimum") {
    assert(everything.copy(minBearers = 231647).keeps(hanna))
    assert(!everything.copy(minBearers = 231648).keeps(hanna))
  }

  test("the second-name filter drops names seldom given as a second name, TYMON too") {
    val men = everything.copy(sex = Sex.Male, alsoSecond = true)
    assert(!men.keeps(oleksandr))
    assert(!men.keeps(tymon))
    assert(men.keeps(xawery))
    assert(everything.copy(alsoSecond = true).keeps(hanna))
    assert(men.copy(alsoSecond = false).keeps(oleksandr))
  }

  test("the alphabet filter drops names with Q, V or X") {
    assert(!everything.copy(polishAlphabet = true).keeps(violetta))
    assert(!everything.copy(sex = Sex.Male, polishAlphabet = true).keeps(xawery))
    assert(everything.copy(polishAlphabet = true).keeps(hanna))
    assert(everything.keeps(violetta))
  }

  test("the page starts with every filter on") {
    assertEquals(NameFilter.default, NameFilter(Sex.Female, minBearers = 100, alsoSecond = true, polishAlphabet = true))
  }
