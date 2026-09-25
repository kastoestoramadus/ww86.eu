package eu.ww86.gen

import java.nio.file.{Files, Path}

/** The data of lab/ursus-by-train, which the page renders itself; its rules are in lab/ursus-by-train.md. */
class UrsusSuite extends munit.FunSuite:

  // sbt runs the tests in the root of the build, where lab/ is
  private val page = Files.readString(Path.of("lab/ursus-by-train/index.html"))

  /** Name and category of every place; stops have no category. */
  private val places = """\{ n:'([^']*)', q:'[^']*', c:'([a-z]+)'""".r
    .findAllMatchIn(page).map(m => m.group(1) -> m.group(2)).toList

  test("every place is read") {
    assertEquals(places.size, "c:'".r.findAllMatchIn(page).size)
  }

  test("every category a place uses has a label, a filter chip, a dot and a colour in every theme") {
    val labels = """var CAT = \{([^}]*)\}""".r.findFirstMatchIn(page).map(_.group(1)).getOrElse("")
    val gaps = for
      c          <- places.map(_._2).distinct
      (what, ok) <- List(
                      "label" -> labels.contains(s"$c:'"),
                      "chip"  -> page.contains(s"""data-cat="$c""""),
                      "dot"   -> page.contains(s".dot.$c{background:var(--c-$c)}"),
                      // light, dark by preference, dark by choice
                      "colour in three themes" -> (s"--c-$c:".r.findAllMatchIn(page).size == 3)
                    )
      if !ok
    yield s"$c: $what"
    assertEquals(gaps, Nil)
  }

  // A cinema's name says what it is.
  private val cinema = "Kino|Multikino|Cinema City|Helios".r
  // Named like a cinema, but screenings are occasional, so it counts as culture.
  private val occasional = Set("Stare Kino")

  test("every cinema with a regular programme is under Kino, and nothing else is") {
    val wrong = places.collect {
      case (name, c) if occasional(name) && c != "kultura"                                     => s"$name: $c"
      case (name, c) if !occasional(name) && cinema.findFirstIn(name).isDefined != (c == "kino") => s"$name: $c"
    }
    assertEquals(wrong, Nil)
  }

  test("a cultural centre is under Kultura, or under Kino when it runs a cinema with a regular programme") {
    val centre = "Centrum Kultury|Ośrodek Kultury|Dom Kultury|Kulturoteka".r
    val wrong = places.collect {
      case (name, c) if centre.findFirstIn(name).isDefined && c != (if cinema.findFirstIn(name).isDefined then "kino" else "kultura") =>
        s"$name: $c"
    }
    assertEquals(wrong, Nil)
  }

  test("every stadium is under Mecze i koncerty") {
    // where you watch a match or a concert; a track you ride yourself is Aktywnie (Arena Pruszków)
    val stadium = "Stadion|Narodowy".r
    val wrong = places.collect { case (name, c) if stadium.findFirstIn(name).isDefined && c != "mecze" => s"$name: $c" }
    assertEquals(wrong, Nil)
  }
