package eu.ww86.gen

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.io.{Codec, Source}
import scala.jdk.CollectionConverters.*

/** Writes the generated pages into the site directory prepared by the `buildSite` sbt task. */
@main def generate(target: String): Unit =
  val root = Path.of(target)
  files.foreach { (relative, content) => write(root.resolve(relative), content) }
  println(s"generated ${files.size} files in $root")

/** Every generated file by its path in the site. */
private[gen] lazy val files: List[(Path, String)] = List(
  Path.of("index.html")                  -> Pages.landing,
  Path.of("copyright.html")              -> Pages.copyright,
  Path.of("lab", "index.html")           -> Pages.labIndex,
  Path.of("lab", "digits", "index.html") -> Pages.digits,
  // the PESEL first names the digits widget fetches; Scala.js cannot read resources
  Path.of("lab", "digits", "names.csv")  -> Source.fromResource("pesel-names.csv")(using Codec.UTF8).mkString
)

private def write(path: Path, page: String): Unit =
  Files.createDirectories(path.getParent)
  Files.writeString(path, page, StandardCharsets.UTF_8)

/** Files under `root` whose names end with one of `extensions`, sorted. */
private[gen] def filesUnder(root: Path, extensions: String*): List[Path] =
  val walk = Files.walk(root)
  try walk.iterator.asScala.filter(p => Files.isRegularFile(p) && extensions.exists(p.toString.endsWith)).toList.sorted
  finally walk.close()
