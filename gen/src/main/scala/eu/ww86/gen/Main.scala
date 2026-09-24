package eu.ww86.gen

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

/** Writes the generated pages into the site directory prepared by the `buildSite` sbt task. */
@main def generate(target: String): Unit =
  val root = Path.of(target)
  val pages = List(
    Path.of("index.html")                -> Pages.landing,
    Path.of("lab", "index.html")         -> Pages.labIndex,
    Path.of("lab", "digits", "index.html") -> Pages.digits
  )
  pages.foreach { (relative, page) => write(root.resolve(relative), page) }
  println(s"generated ${pages.size} pages in $root")

private def write(path: Path, page: String): Unit =
  Files.createDirectories(path.getParent)
  Files.writeString(path, page, StandardCharsets.UTF_8)

/** Files under `root` whose names end with one of `extensions`, sorted. */
private[gen] def filesUnder(root: Path, extensions: String*): List[Path] =
  val walk = Files.walk(root)
  try walk.iterator.asScala.filter(p => Files.isRegularFile(p) && extensions.exists(p.toString.endsWith)).toList.sorted
  finally walk.close()
