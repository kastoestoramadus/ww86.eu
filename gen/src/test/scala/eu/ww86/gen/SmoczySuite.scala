package eu.ww86.gen

import java.nio.file.{Files, Path}

/** lab/smoczy-pokedex re-imagines third-party characters. Until their rights are shown, the page publishes no
  * illustration prompts and no pictures: only the empty slots and the status of each one.
  */
class SmoczySuite extends munit.FunSuite:

  // sbt runs the tests in the root of the build, where lab/ is
  private val dir     = Path.of("lab/smoczy-pokedex")
  private val catalog = Files.readString(dir.resolve("data/catalog.json"))

  test("no prompt text is published, neither in the catalog nor as an export") {
    assert(!catalog.contains("\"prompt_text\""), "catalog.json carries prompt_text")
    assert(!Files.exists(dir.resolve("exports")), "lab/smoczy-pokedex/exports/ is published")
  }

  test("no picture is published: every image slot is absent and the page directory holds no image") {
    assertEquals("\"image_status\": \"(?!absent\")".r.findFirstIn(catalog), None)
    val images = Files.walk(dir).toArray.map(_.toString).filter(_.matches("(?i).*\\.(png|jpe?g|webp|gif|svg|avif)")).toList
    assertEquals(images, Nil)
  }
