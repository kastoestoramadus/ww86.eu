package eu.ww86.gen

import eu.ww86.digits.{GivenName, Sex}
import java.nio.file.Path

class FilesSuite extends munit.FunSuite:

  test("the PESEL names are written next to the digits page, which fetches them by a relative path") {
    val names = files.toMap.get(Path.of("lab", "digits", "names.csv")).map(GivenName.parse).getOrElse(Nil)
    assert(names.size > 10000, names.size)
    assertEquals(names.find(_.name == "ANNA").map(_.sex), Some(Sex.Female))
  }
