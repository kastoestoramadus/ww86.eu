package eu.ww86.gen

import java.nio.file.{Files, Path}

/** The data of lab/ursus-by-train, which the page renders itself; its rules are in lab/ursus-by-train.md. */
class UrsusSuite extends munit.FunSuite:

  // sbt runs the tests in the root of the build, where lab/ is
  private val page = Files.readString(Path.of("lab/ursus-by-train/index.html"))

  /** A place of the page; stops have no category. `season` says what runs part of the year only, and when. */
  private case class Place(name: String, cats: List[String], text: String, season: Option[String])

  private val places = """\{ n:'([^']*)', q:'[^']*', c:\[([^\]]*)\], d:[^,]*, t:'([^']*)'(?:, s:'([^']*)')?""".r
    .findAllMatchIn(page)
    .map(m => Place(m.group(1), "'([a-z]+)'".r.findAllMatchIn(m.group(2)).map(_.group(1)).toList, m.group(3), Option(m.group(4))))
    .toList

  test("every place is read") {
    assertEquals(places.size, " c:".r.findAllMatchIn(page).size)
  }

  test("a place has one or two categories, the most important first, none twice") {
    val wrong = places.filter(p => p.cats.isEmpty || p.cats.size > 2 || p.cats.distinct != p.cats).map(p => s"${p.name}: ${p.cats}")
    assertEquals(wrong, Nil)
  }

  test("every category a place uses has a label, a filter chip, a dot and a colour in every theme") {
    val labels = """var CAT = \{([^}]*)\}""".r.findFirstMatchIn(page).map(_.group(1)).getOrElse("")
    val gaps = for
      c          <- places.flatMap(_.cats).distinct
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

  test("every cinema with a regular programme is first under Kino, and nothing else is under it") {
    val wrong = places.collect {
      case Place(name, cats, _, _) if occasional(name) && (!cats.contains("kultura") || cats.contains("kino")) => s"$name: $cats"
      case Place(name, cats, _, _) if !occasional(name) && cinema.findFirstIn(name).isDefined != cats.contains("kino") =>
        s"$name: $cats"
      case Place(name, cats, _, _) if cats.contains("kino") && cats.head != "kino" => s"$name: $cats"
    }
    assertEquals(wrong, Nil)
  }

  test("a cultural centre is under Kultura, and first under Kino when it runs a cinema with a regular programme") {
    val centre = "Centrum Kultury|Ośrodek Kultury|Dom Kultury|Kulturoteka".r
    val wrong = places.collect {
      case Place(name, cats, _, _) if centre.findFirstIn(name).isDefined &&
            (if cinema.findFirstIn(name).isDefined then cats != List("kino", "kultura") else !cats.contains("kultura")) =>
        s"$name: $cats"
    }
    assertEquals(wrong, Nil)
  }

  test("every stadium is under Mecze i koncerty") {
    // where you watch a match or a concert; a track you ride yourself is Aktywnie (Arena Pruszków)
    val stadium = "Stadion|Narodowy".r
    val wrong = places.collect { case Place(name, cats, _, _) if stadium.findFirstIn(name).isDefined && !cats.contains("mecze") => s"$name: $cats" }
    assertEquals(wrong, Nil)
  }

  private type Point = (Double, Double)

  /** A route column of the page; `stops` are (name, q), and a bus stop's q is its coordinates. */
  private case class Line(id: String, bus: Boolean, stops: List[(String, String)])

  private def block(name: String): String =
    val start = page.indexOf(s"var $name = [")
    page.substring(start, page.indexOf("\n  ];", start))

  // An entry opens at four spaces, its stops at six, their places deeper.
  private val lines = List("LINES" -> false, "BUSES" -> true, "RARE" -> false).flatMap { (name, bus) =>
    block(name).split("\n    \\{ id:'").toList.tail.map { entry =>
      val stops = """(?m)^      \{ n:'([^']*)', q:'([^']*)'""".r.findAllMatchIn(entry).map(m => m.group(1) -> m.group(2))
      Line(entry.takeWhile(_ != '\''), bus, stops.toList)
    }
  }

  /** Po drodze w Warszawie: each station with the lines on its card. */
  private val hub = """(?m)^    \{ n:'([^']*)', q:'[^']*', lines:\[([^\]]*)\]""".r.findAllMatchIn(block("HUB")).map { m =>
    m.group(1) -> m.group(2).split(",").map(_.trim.stripPrefix("'").stripSuffix("'")).toList
  }.toList

  /** A route on the map: the stations a train calls at, drawn on its path, and the path. */
  private case class Route(stops: List[(String, Point)], path: List[Point])

  // written by lab/ursus-by-train.py --map
  private val routesFile = Path.of("lab/ursus-by-train/routes.js")
  private val routes: Map[String, Route] =
    val text = if Files.exists(routesFile) then Files.readString(routesFile) else ""
    """(?m)^  (\w+): \{\n((?:    .*\n)+)  \}""".r.findAllMatchIn(text).map { m =>
      def line(key: String) = m.group(2).linesIterator.find(_.trim.startsWith(s"$key:")).getOrElse("")
      val stops = """\["([^"]+)",(-?[\d.]+),(-?[\d.]+)\]""".r.findAllMatchIn(line("stops"))
        .map(s => s.group(1) -> (s.group(2).toDouble, s.group(3).toDouble))
      val path = """\[(-?[\d.]+),(-?[\d.]+)\]""".r.findAllMatchIn(line("path"))
        .map(p => (p.group(1).toDouble, p.group(2).toDouble))
      m.group(1) -> Route(stops.toList, path.toList)
    }.toMap

  /** Metres from `p` to the nearest point of `path`, on a plane: close enough at this scale. */
  private def metres(p: Point, path: List[Point]): Double =
    val k            = math.cos(math.toRadians(52.2))
    def xy(q: Point) = (q._2 * k * 111320, q._1 * 111320)
    val (px, py)     = xy(p)
    val segments     = if path.size == 1 then List(path.head, path.head) else path
    segments.map(xy).sliding(2).collect { case List((ax, ay), (bx, by)) =>
      val (dx, dy) = (bx - ax, by - ay)
      val squared  = dx * dx + dy * dy
      val t        = if squared == 0 then 0.0 else (((px - ax) * dx + (py - ay) * dy) / squared).max(0).min(1)
      math.hypot(px - ax - t * dx, py - ay - t * dy)
    }.min

  test("every line on the page has its route on the map") {
    assertEquals(lines.map(_.id).filterNot(routes.contains), Nil)
  }

  test("a train's route calls at the stations of its line, in the page's order") {
    val wrong = for
      l <- lines if !l.bus
      names = l.stops.map(_._1)
      onMap = routes.get(l.id).toList.flatMap(_.stops.map(_._1))
      if onMap.filter(names.contains) != names
    yield s"${l.id}: ${names.filterNot(onMap.contains).mkString(", ")}"
    assertEquals(wrong, Nil)
  }

  test("a train with a Po drodze station on its card calls there on the map") {
    val trains = lines.filterNot(_.bus).map(_.id).toSet
    val wrong = for
      (station, ids) <- hub
      id             <- ids if trains(id) && !routes.get(id).exists(_.stops.exists(_._1 == station))
    yield s"$id: $station"
    assertEquals(wrong, Nil)
  }

  test("every route runs past each stop of its line and ends at the last one") {
    // a bus stop is where the page puts it, a station where the map draws it
    def coordinates(q: String): Point = q.split(",") match { case Array(lat, lon) => (lat.toDouble, lon.toDouble) }
    val wrong = for
      l      <- lines
      r      <- routes.get(l.id).toList if r.path.nonEmpty
      points  = l.stops.flatMap((n, q) => if l.bus then Some(coordinates(q)) else r.stops.collectFirst { case (`n`, p) => p })
      (p, i) <- points.zipWithIndex
      off     = metres(p, if i == points.size - 1 then List(r.path.last) else r.path)
      if off > 50
    yield f"${l.id}, stop ${i + 1}: $off%.0f m"
    assertEquals(wrong, Nil)
  }

  test("a chain has at most three big stores on the page, and besides them its biggest within 30 minutes' ride") {
    // A place counts for every chain its name or text names, unless as a supermarket (the Carrefour in Centrum Skorosze);
    // the one over three says it is the chain's biggest within 30 minutes.
    val chains = List("Kaufland", "Carrefour", "Auchan", "E.Leclerc", "Lidl", "Biedronka", "Netto", "Aldi", "Intermarché", "Stokrotka",
      "Selgros", "Eurospar")
    val over = for
      chain  <- chains
      named   = s"(?<!supermarket )${java.util.regex.Pattern.quote(chain)}".r
      stores  = places.filter(p => p.cats.contains("zakupy") && named.findFirstIn(s"${p.name} ${p.text}").isDefined)
      biggest = stores.exists(p => p.text.contains("największy") && p.text.contains("30 minut"))
      if stores.size > (if biggest then 4 else 3)
    yield s"$chain: ${stores.size}"
    assertEquals(over, Nil)
  }

  test("Aldi and Netto are on the page; Lidl, Biedronka and Stokrotka are not, the user walks to those") {
    val names   = places.map(_.name)
    val missing = List("Aldi", "Netto").filterNot(chain => names.exists(_.contains(chain)))
    val walked  = names.filter(name => List("Lidl", "Biedronka", "Stokrotka").exists(name.contains))
    assertEquals(missing ++ walked, Nil)
  }

  test("a sauna is in bold wherever a text names one") {
    // **…** is the only markup a text has
    val wrong = places.filter(p => "(?i)saun".r.findFirstIn(p.text.replaceAll("""\*\*[^*]+\*\*""", "")).isDefined).map(_.name)
    assertEquals(wrong, Nil)
  }

  test("a bathing beach, an outdoor pool or a rink says when it runs") {
    val seasonal = "(?i)kąpielisk|odkryt|zewnętrzn|ślizgawk|lodowisk".r
    val wrong    = places.filter(p => seasonal.findFirstIn(p.text).isDefined && p.season.isEmpty).map(_.name)
    assertEquals(wrong, Nil)
  }
