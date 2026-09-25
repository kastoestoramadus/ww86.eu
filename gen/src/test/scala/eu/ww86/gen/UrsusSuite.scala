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

  test("every cinema is under Kino, and nothing else is") {
    // A cinema's name says what it is; a cultural centre that runs one has a separate entry for it.
    val cinema = "Kino|Multikino|Cinema City|Helios".r
    val wrong = places.collect { case (name, c) if cinema.findFirstIn(name).isDefined != (c == "kino") => s"$name: $c" }
    assertEquals(wrong, Nil)
  }
